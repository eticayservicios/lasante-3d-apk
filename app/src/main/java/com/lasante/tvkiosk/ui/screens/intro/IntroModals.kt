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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.lasante.tvkiosk.ui.layout.DeviceProfileResolver
import com.lasante.tvkiosk.ui.layout.DeviceProfileTier
import com.lasante.tvkiosk.ui.layout.TvProfileDetector
import com.lasante.tvkiosk.ui.theme.LaSanteBlue
import com.lasante.tvkiosk.ui.theme.LaSanteText

/** QR para interacción móvil en tienda. Preferir llamar desde [Dispatchers.Default]. */
fun generateQrBitmap(url: String, size: Int = 512): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 1)
    val bits = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints)
    val pixels = IntArray(size * size)
    var i = 0
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[i++] =
                if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
        it.setPixels(pixels, 0, size, 0, 0, size, size)
    }
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

    val density = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    // Usar tamaño de pantalla (no constraints del Dialog): ahí fallaba el scale de TV66.
    val screenW = configuration.screenWidthDp.dp
    val screenH = configuration.screenHeightDp.dp
    val preferTv66 = TvProfileDetector.isTv66Candidate(
        maxWidth = screenW,
        maxHeight = screenH,
        density = density,
        context = context,
    )
    val isTv66 = remember(screenW, screenH, preferTv66) {
        DeviceProfileResolver.resolve(
            maxWidth = screenW,
            maxHeight = screenH,
            preferTv66 = preferTv66,
        ).tier == DeviceProfileTier.TV_LARGE
    }
    // TV66 (TV66 ref. 1280×720): ~22.4% del ancho.
    val qrSize = if (isTv66) {
        (screenW * 0.224f).coerceIn(256.dp, 448.dp)
    } else {
        168.dp
    }
    val scale = if (isTv66) qrSize.value / 168f else 1f
    val hPad = 8.dp * scale
    val vPad = 6.dp * scale
    val corner = 14.dp * scale
    val closeBtn = 26.dp * scale
    val closeIcon = 16.dp * scale
    val titleSize = 15.sp * scale
    val captionSize = 11.sp * scale

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClose() }
            .zIndex(140f),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(corner),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = hPad, vertical = vPad),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.width(qrSize),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        color = LaSanteBlue,
                        maxLines = 1,
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(closeBtn),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(closeIcon),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp * scale))
                var qrBitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
                LaunchedEffect(url) {
                    qrBitmap = withContext(Dispatchers.Default) { generateQrBitmap(url) }
                }
                qrBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(qrSize),
                    )
                } ?: Box(modifier = Modifier.size(qrSize))
                Spacer(modifier = Modifier.height(4.dp * scale))
                Text(
                    text = "Escanea para abrir $label",
                    fontSize = captionSize,
                    color = LaSanteText,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(qrSize),
                    textAlign = TextAlign.Center,
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
                Lifecycle.Event.ON_START,
                Lifecycle.Event.ON_RESUME,
                -> {
                    exoPlayer.playWhenReady = true
                }
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP,
                -> {
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
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
