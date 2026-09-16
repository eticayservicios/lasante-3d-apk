package com.lasante.tvkiosk.ui.screens.intro

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.imageLoader
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import kotlinx.coroutines.yield

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
            url = "https://www.instagram.com/lasante.ve",
            iconAssetPath = SocialNetworkAssets.INSTAGRAM,
        ),
        SocialNetwork(
            id = SocialNetworkId.Facebook,
            label = "Facebook",
            url = "https://www.facebook.com/lasante.ve/?locale=es_LA",
            iconAssetPath = SocialNetworkAssets.FACEBOOK,
        ),
        SocialNetwork(
            id = SocialNetworkId.LinkedIn,
            label = "LinkedIn",
            url = "https://ve.linkedin.com/company/lasante-venezuela",
            iconAssetPath = SocialNetworkAssets.LINKEDIN,
        ),
    )
}

/**
 * Precarga los GIF de redes y los arranca juntos para que el loop quede sincronizado
 * (mismos frames/duración en assets; Coil por sí solo arranca al terminar cada decode).
 */
@Composable
fun rememberSyncedSocialGifDrawables(
    socialNetworks: List<SocialNetwork>,
    sizePx: Int,
): Map<SocialNetworkId, Drawable> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var drawables by remember(socialNetworks, sizePx) {
        mutableStateOf<Map<SocialNetworkId, Drawable>>(emptyMap())
    }

    fun restartAll(map: Map<SocialNetworkId, Drawable>) {
        map.values.forEach { drawable ->
            (drawable as? Animatable)?.stop()
        }
        map.values.forEach { drawable ->
            (drawable as? Animatable)?.start()
        }
    }

    LaunchedEffect(socialNetworks, sizePx) {
        val loader = context.imageLoader
        val loaded = LinkedHashMap<SocialNetworkId, Drawable>(socialNetworks.size)
        for (social in socialNetworks) {
            val exists = runCatching {
                context.assets.open(social.iconAssetPath).close()
                true
            }.getOrDefault(false)
            if (!exists) continue
            val result = loader.execute(
                VitrinaUiImages.request(
                    context,
                    SocialNetworkAssets.assetUri(social.iconAssetPath),
                    sizePx = sizePx,
                ),
            )
            val drawable = result.drawable ?: continue
            (drawable as? Animatable)?.stop()
            loaded[social.id] = drawable
        }
        drawables = loaded
        // Misma “marca” de arranque tras componer los painters.
        yield()
        restartAll(loaded)
    }

    // Al volver de background, Coil puede dejar animaciones desfasadas: re-sync.
    DisposableEffect(lifecycleOwner, drawables) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && drawables.isNotEmpty()) {
                restartAll(drawables)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(drawables) {
        if (drawables.isEmpty()) return@LaunchedEffect
        yield()
        restartAll(drawables)
    }

    return drawables
}

@Composable
fun SocialNetworkIconButton(
    social: SocialNetwork,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    syncedDrawable: Drawable? = null,
) {
    val context = LocalContext.current
    val hasCustomIcon = syncedDrawable != null || remember(social.iconAssetPath) {
        runCatching {
            context.assets.open(social.iconAssetPath).close()
            true
        }.getOrDefault(false)
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
                            colors = listOf(Color(0xFF2FAD11), Color(0xFF2FAD11)),
                        ),
                    )
                },
            )
            .clickableWithSound { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        when {
            syncedDrawable != null -> AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply {
                        contentDescription = social.label
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        adjustViewBounds = true
                        setImageDrawable(syncedDrawable)
                    }
                },
                update = { imageView ->
                    imageView.contentDescription = social.label
                    if (imageView.drawable !== syncedDrawable) {
                        imageView.setImageDrawable(syncedDrawable)
                    }
                },
                modifier = Modifier.size(size),
            )
            hasCustomIcon -> {
                // Esperando sync: placeholder vacío (evita un GIF arrancando solo).
                Box(modifier = Modifier.size(size))
            }
            else -> SocialNetworkFallbackGlyph(id = social.id, size = size)
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
