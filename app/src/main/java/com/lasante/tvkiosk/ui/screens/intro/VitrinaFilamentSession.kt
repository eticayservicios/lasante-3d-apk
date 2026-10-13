package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.filament.Engine
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.engine
import io.github.sceneview.model.model
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine, context)
    val baseGlbSource = rememberVitrinaBaseGlbSource()
    var baseInstance by remember(baseGlbSource.loadPath) {
        mutableStateOf<ModelInstance?>(null)
    }

    LaunchedEffect(modelLoader, baseGlbSource.loadPath) {
        // Ceder el hilo para que Intro (spinner) pinte antes de parsear el GLB.
        yield()
        delay(32)
        // Parsear en IO: el parse de Draco 24 MB bloquea el main thread en tablets lentas
        // el tiempo suficiente para que Android dispare el diálogo ANR.
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                modelLoader.loadModelInstance(baseGlbSource.loadPath)
            }.onFailure { t ->
                VitrinaDebugLog.e("VitrinaGlb", "load ${baseGlbSource.loadPath} failed", t)
            }.getOrNull()
        }
        loaded?.hideDecorativeBackgroundPlanes()
        baseInstance = loaded
        VitrinaDebugLog.d(
            "VitrinaGlb",
            "session base asset=${baseGlbSource.loadPath} loaded=${loaded != null}",
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
