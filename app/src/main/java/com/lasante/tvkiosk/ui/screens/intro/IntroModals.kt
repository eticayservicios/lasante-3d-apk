package com.lasante.tvkiosk.ui.screens.intro

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.data.ScreenSaverVideo
import com.lasante.tvkiosk.data.ScreenSaverVideoPolicy
import com.lasante.tvkiosk.media.VideoCache
import com.lasante.tvkiosk.ui.components.ProductPresentationModal
import com.lasante.tvkiosk.ui.theme.LaSanteBlue
import com.lasante.tvkiosk.ui.theme.LaSanteText

// Generador de QR para interacción con el móvil en tienda
fun generateQrBitmap(url: String, size: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bits = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp
}

@Composable
fun IntroProductDetailModal(
    product: Product,
    widthClass: WindowWidthSizeClass,
    onStopAudio: () -> Unit,
    onClose: () -> Unit,
) {
    DisposableEffect(Unit) {
        onDispose { onStopAudio() }
    }

    ProductPresentationModal(
        product = product,
        widthClass = widthClass,
        onClose = {
            onStopAudio()
            onClose()
        },
    )
}

@Composable
fun VideoPlayerModal(
    videoUrl: String?,
    onClose: () -> Unit,
    widthClass: WindowWidthSizeClass,
) {
    val resolvedUrl = videoUrl?.trim()?.takeIf { it.isNotBlank() }
    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
            .zIndex(150f),
        contentAlignment = Alignment.Center,
    ) {
        if (resolvedUrl == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
            ) {
                Text(
                    "Video institucional no disponible",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Configura el Video 1 en Admin > Vitrina > Videos.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }
        } else {
            ExoVideoPlayer(
                urls = listOf(resolvedUrl),
                modifier = Modifier
                    .fillMaxWidth(if (widthClass == WindowWidthSizeClass.Compact) 0.96f else 0.88f)
                    .fillMaxHeight(0.82f),
                repeatMode = Player.REPEAT_MODE_OFF,
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }
    }
}

@Composable
fun ScreenSaverVideoOverlay(
    videos: List<ScreenSaverVideo>,
    onClose: () -> Unit,
) {
    val urls = remember(videos) {
        ScreenSaverVideoPolicy.filterPlaylist(videos)
            .sortedBy { it.order }
            .map { it.url.trim() }
            .filter { it.isNotBlank() }
    }
    if (urls.isEmpty()) return

    android.util.Log.i("VitrinaScreenSaver", "Reproduciendo ${urls.size} videos de screen saver")

    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
            .zIndex(120f),
        contentAlignment = Alignment.Center,
    ) {
        ExoVideoPlayer(
            urls = urls,
            modifier = Modifier.fillMaxSize(),
            repeatMode = Player.REPEAT_MODE_ALL,
        )
    }
}

@Composable
fun IntroSocialQrModal(
    label: String,
    url: String,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
            .zIndex(140f),
        contentAlignment = Alignment.Center,
    ) {
        // Cuadrado en todos los perfiles (ya no landscape ancho). QR +10% vs 220.dp.
        val qrSize = 242.dp
        Card(
            modifier = Modifier
                .fillMaxHeight(0.78f)
                .aspectRatio(1f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = LaSanteBlue,
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val qrBitmap = remember(url) { generateQrBitmap(url) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(qrSize),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Escanea para abrir $label",
                    fontSize = 15.sp,
                    color = LaSanteText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExoVideoPlayer(
    urls: List<String>,
    modifier: Modifier = Modifier,
    repeatMode: Int = Player.REPEAT_MODE_ALL,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember(urls, repeatMode) {
        ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(VideoCache.dataSourceFactory(appContext)),
            )
            .build()
            .apply {
            this.repeatMode = repeatMode
            setMediaItems(urls.map { MediaItem.fromUri(VideoCache.normalizeVideoUri(it)) })
            addListener(
                object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        android.util.Log.e(
                            "VitrinaScreenSaver",
                            "Error reproduciendo video index=$currentMediaItemIndex url=${urls.getOrNull(currentMediaItemIndex)}: ${error.message}",
                            error,
                        )
                        if (currentMediaItemIndex < mediaItemCount - 1) {
                            seekTo(currentMediaItemIndex + 1, 0L)
                            prepare()
                            playWhenReady = true
                            play()
                        }
                    }
                },
            )
            prepare()
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    exoPlayer.playWhenReady = true
                }
                Lifecycle.Event.ON_STOP -> {
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            exoPlayer.playWhenReady = true
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        onRelease = { playerView -> playerView.player = null },
        modifier = modifier,
    )
}
