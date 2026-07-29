package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.filament.Engine
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.engine
import io.github.sceneview.model.model
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

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
    val baseInstance = rememberModelInstance(
        modelLoader,
        assetFileLocation = baseGlbSource.loadPath,
    )

    LaunchedEffect(baseGlbSource, baseInstance) {
        baseInstance?.hideDecorativeBackgroundPlanes()
        VitrinaDebugLog.d(
            "VitrinaGlb",
            "session base asset=${baseGlbSource.loadPath} loaded=${baseInstance != null}",
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
        val entity = model.getFirstEntityByName(nodeName)
        if (entity != 0 && renderableManager.hasComponent(entity)) {
            renderableManager.setLayerMask(renderableManager.getInstance(entity), 0xff, 0x00)
            VitrinaDebugLog.d("VitrinaGlb", "hidden decorative plane=$nodeName")
        }
    }
}
