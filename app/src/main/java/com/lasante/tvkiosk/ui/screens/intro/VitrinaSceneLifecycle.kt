package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Lifecycle controlado para [SceneView]: pausa el loop de Filament cuando la vitrina
 * está oculta (navegación), con modal abierto o en screen saver — sin desmontar la escena.
 */
private class ControllableLifecycleOwner : LifecycleOwner {
    val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
}

@Composable
fun rememberVitrinaSceneLifecycle(renderingEnabled: Boolean): Lifecycle {
    val owner = remember {
        ControllableLifecycleOwner().apply {
            registry.currentState = Lifecycle.State.CREATED
        }
    }
    DisposableEffect(Unit) {
        onDispose { owner.registry.currentState = Lifecycle.State.DESTROYED }
    }
    SideEffect {
        owner.registry.currentState = if (renderingEnabled) {
            Lifecycle.State.RESUMED
        } else {
            Lifecycle.State.STARTED
        }
    }
    return owner.lifecycle
}
