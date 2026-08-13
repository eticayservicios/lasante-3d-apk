package com.lasante.tvkiosk.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext

fun Modifier.clickableWithSound(
    enabled: Boolean = true,
    sound: UiSound = UiSound.Click,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val context = LocalContext.current
    this.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            SoundManager.play(context, sound)
            onClick()
        },
    )
}
