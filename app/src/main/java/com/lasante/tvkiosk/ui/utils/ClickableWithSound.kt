package com.lasante.tvkiosk.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext

fun Modifier.clickableWithSound(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onClick: () -> Unit
): Modifier = composed {
    val context = LocalContext.current
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            SoundManager.playClickSound(context)
            onClick()
        }
    )
}
