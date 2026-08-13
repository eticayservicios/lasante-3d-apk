package com.lasante.tvkiosk.ui.screens.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lasante.tvkiosk.ui.utils.clickableWithSound

/** Rutas de GIF por red social dentro de `assets/vitrina/ui/`. */
object SocialNetworkAssets {
    private const val BASE = "vitrina/ui"

    const val INSTAGRAM = "$BASE/social_instagram.gif"
    const val FACEBOOK = "$BASE/social_facebook.gif"
    const val LINKEDIN = "$BASE/social_linkedin.gif"

    fun assetUri(relativePath: String): String =
        "file:///android_asset/$relativePath"
}

enum class SocialNetworkId {
    Instagram,
    Facebook,
    LinkedIn,
}

data class SocialNetwork(
    val id: SocialNetworkId,
    val label: String,
    val url: String,
    /** Ruta relativa en assets; si el PNG no existe, se muestra fallback programado. */
    val iconAssetPath: String,
)

object SocialNetworks {
    val defaults: List<SocialNetwork> = listOf(
        SocialNetwork(
            id = SocialNetworkId.Instagram,
            label = "Instagram",
            url = "https://www.instagram.com/pharmetiquelabs.ve/",
            iconAssetPath = SocialNetworkAssets.INSTAGRAM,
        ),
        SocialNetwork(
            id = SocialNetworkId.Facebook,
            label = "Facebook",
            url = "https://www.facebook.com/pharmetiquelabs.ve",
            iconAssetPath = SocialNetworkAssets.FACEBOOK,
        ),
        SocialNetwork(
            id = SocialNetworkId.LinkedIn,
            label = "LinkedIn",
            url = "https://www.linkedin.com/company/pharmetique-labs-venezuela/",
            iconAssetPath = SocialNetworkAssets.LINKEDIN,
        ),
    )
}

@Composable
fun SocialNetworkIconButton(
    social: SocialNetwork,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasCustomIcon = remember(social.iconAssetPath) {
        runCatching {
            context.assets.open(social.iconAssetPath).close()
            true
        }.getOrDefault(false)
    }
    val iconModel = remember(social.iconAssetPath, context) {
        VitrinaUiImages.request(context, SocialNetworkAssets.assetUri(social.iconAssetPath))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (hasCustomIcon) {
                    Modifier
                } else {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF68BD45), Color(0xFF319D32)),
                        ),
                    )
                },
            )
            .clickableWithSound { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (hasCustomIcon) {
            AsyncImage(
                model = iconModel,
                contentDescription = social.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size),
            )
        } else {
            SocialNetworkFallbackGlyph(id = social.id, size = size)
        }
    }
}

@Composable
private fun SocialNetworkFallbackGlyph(id: SocialNetworkId, size: Dp) {
    when (id) {
        SocialNetworkId.Instagram -> Text(
            text = "ig",
            color = Color.White,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.ExtraBold,
        )
        SocialNetworkId.Facebook -> Icon(
            imageVector = Icons.Default.Facebook,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.52f),
        )
        SocialNetworkId.LinkedIn -> Text(
            text = "in",
            color = Color.White,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
