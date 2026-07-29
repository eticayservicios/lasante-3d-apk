package com.lasante.tvkiosk.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode

/**
 * Visualizador 3D de producto (assets locales o URLs remotas CloudFront).
 */
@Composable
fun ModelViewerStub(
    modifier: Modifier = Modifier,
    modelUrl: String?,
    scaleToUnits: Float = 1.0f,
) {
    if (modelUrl.isNullOrBlank()) return

    val cleanPath = remember(modelUrl) {
        when {
            modelUrl.startsWith("file:///android_asset/") -> modelUrl.removePrefix("file:///android_asset/")
            modelUrl.startsWith("./") -> modelUrl.removePrefix("./")
            else -> modelUrl.trim()
        }
    }

    val context = LocalContext.current
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine, context)
    val modelInstance = rememberProductModelInstance(modelLoader, cleanPath)
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0f, z = 2.8f)
        lookAt(Position(x = 0f, y = 0f, z = 0f), smooth = false)
    }
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000f
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            isOpaque = false,
            autoCenterContent = true,
            autoFitContent = true,
            cameraNode = cameraNode,
            mainLightNode = mainLightNode,
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    centerOrigin = Position(x = 0f, y = -1f, z = 0f),
                    scaleToUnits = scaleToUnits,
                    isEditable = false,
                )
            }
        }

        if (modelInstance == null) {
            CircularProgressIndicator(color = LaSanteGreen)
        }
    }
}

@Composable
private fun rememberProductModelInstance(
    modelLoader: io.github.sceneview.loaders.ModelLoader,
    path: String,
) = when {
    path.startsWith("http://") || path.startsWith("https://") ->
        rememberModelInstance(modelLoader, fileLocation = path)
    else ->
        rememberModelInstance(modelLoader, assetFileLocation = path)
}
