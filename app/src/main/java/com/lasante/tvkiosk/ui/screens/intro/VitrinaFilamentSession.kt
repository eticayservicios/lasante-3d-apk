package com.lasante.tvkiosk.ui.screens.intro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext
import com.google.android.filament.Engine
import com.lasante.tvkiosk.app.FilamentNativeSupport
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.engine
import io.github.sceneview.model.model
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

/** Filament compartido por IntroScreen — sobrevive desmontes puntuales del viewer. */
class VitrinaFilamentSession internal constructor(
    val engine: Engine,
    val modelLoader: ModelLoader,
    val baseGlbSource: VitrinaBaseGlbSource,
    val baseInstance: ModelInstance?,
)

@Composable
fun rememberVitrinaFilamentSession(): VitrinaFilamentSession {
    val context = LocalContext.current
    val engine = rememberEngine { eglContext ->
        if (FilamentNativeSupport.requiresVulkanBackend()) {
            Engine.create(Engine.Backend.VULKAN)
        } else {
            Engine.create(eglContext)
        }
    }
    val modelLoader = rememberModelLoader(engine, context)
    val baseGlbSource = rememberVitrinaBaseGlbSource()
    var baseInstance by remember(baseGlbSource.loadPath) {
        mutableStateOf<ModelInstance?>(null)
    }

    LaunchedEffect(modelLoader, baseGlbSource.loadPath) {
        // Ceder el hilo para que Intro (spinner) pinte antes de parsear el GLB.
        // No usar withContext(IO): ModelLoader internamente alterna IO (lectura de assets)
        // y Main (upload a Filament/GPU); envolverlo en IO bloquea el switch a Main.
        // El yield() + delay garantizan que el primer frame se pinte antes del parse.
        yield()
        val loadDelayMs = VitrinaDeviceLoadPolicy.baseGlbLoadDelayMs(context)
        delay(loadDelayMs)
        val loaded = try {
            withWindowInputPaused(context) {
                modelLoader.loadModelInstance(baseGlbSource.loadPath)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (t: Throwable) {
            VitrinaDebugLog.e("VitrinaGlb", "load ${baseGlbSource.loadPath} failed", t)
            null
        }
        loaded?.hideDecorativeBackgroundPlanes()
        loaded?.lowerFloatingPediatricBox()
        baseInstance = loaded
        VitrinaDebugLog.d(
            "VitrinaGlb",
            "session base asset=${baseGlbSource.loadPath} loaded=${loaded != null} delayMs=$loadDelayMs",
        )
    }

    return remember(engine, modelLoader, baseGlbSource, baseInstance) {
        VitrinaFilamentSession(
            engine = engine,
            modelLoader = modelLoader,
            baseGlbSource = baseGlbSource,
            baseInstance = baseInstance,
        )
    }
}

/**
 * SceneView crea el modelo de Filament en Main. Mientras esa llamada nativa no
 * retorna, impedir nuevos toques evita que InputDispatcher abra un evento que
 * termine en ANR. Esperamos dos frames para que WindowManager aplique el flag.
 */
private suspend fun <T> withWindowInputPaused(
    context: Context,
    block: suspend () -> T,
): T {
    val window = context.findActivity()?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    return try {
        if (window != null) {
            repeat(2) { withFrameNanos { } }
        }
        block()
    } finally {
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Ajuste puntual del arte base: la caja pediátrica de la repisa superior quedó
 * separada de la superficie. El padre `Empty` convierte Z local en el eje
 * vertical invertido, por eso aumentar Z baja la caja visualmente.
 */
private fun ModelInstance.lowerFloatingPediatricBox() {
    val entity = model.getFirstEntityByName("Cube.003")
    if (entity == 0) return

    val transformManager = engine.transformManager
    if (!transformManager.hasComponent(entity)) return

    val instance = transformManager.getInstance(entity)
    val transform = FloatArray(16)
    transformManager.getTransform(instance, transform)
    transform[14] += 0.015f
    transformManager.setTransform(instance, transform)
}

private fun ModelInstance.hideDecorativeBackgroundPlanes() {
    val renderableManager = engine.renderableManager
    listOf("Plane", "Background", "Grid").forEach { nodeName ->
        runCatching {
            val entity = model.getFirstEntityByName(nodeName)
            if (entity != 0 && renderableManager.hasComponent(entity)) {
                renderableManager.setLayerMask(renderableManager.getInstance(entity), 0xff, 0x00)
            }
        }
    }
}
