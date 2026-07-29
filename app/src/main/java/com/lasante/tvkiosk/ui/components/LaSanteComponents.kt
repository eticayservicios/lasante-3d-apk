package com.lasante.tvkiosk.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.lasante.tvkiosk.ui.theme.LaSanteBackground
import com.lasante.tvkiosk.ui.utils.SoundManager
import com.lasante.tvkiosk.ui.theme.LaSanteGreen
import com.lasante.tvkiosk.ui.theme.LaSanteText
import androidx.compose.ui.text.TextStyle 

@Composable
fun LaSanteBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaSanteBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val path = Path().apply {
                moveTo(width * 0.7f, 0f)
                lineTo(width, 0f)
                lineTo(width, height * 0.4f)
                close()
            }
            drawPath(path, color = Color(0xFFE0E0E0).copy(alpha = 0.3f))

            val path2 = Path().apply {
                moveTo(0f, height * 0.6f)
                lineTo(0f, height)
                lineTo(width * 0.3f, height)
                close()
            }
            drawPath(path2, color = Color(0xFFE0E0E0).copy(alpha = 0.3f))
        }
        content()
    }
}

@Composable
fun GreenNavButton(
    assetPath: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .padding(top = 2.dp)
            .size(size)
            .clickable {
                SoundManager.playClickSound(context)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/$assetPath")
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun LaSanteNavigationHeader(
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val buttonSize = if (isLandscape) 40.dp else 34.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isLandscape) 36.dp else 22.dp,
                vertical = if (isLandscape) 18.dp else 14.dp
            ),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 14.dp else 10.dp)
    ) {
        GreenNavButton(
            assetPath = "svg/ui/Before.svg",
            contentDescription = "Volver",
            onClick = onBack,
            size = buttonSize
        )
        GreenNavButton(
            assetPath = "svg/ui/Home.svg",
            contentDescription = "Inicio",
            onClick = onHome,
            size = buttonSize
        )
        content()
    }
}

// Header estándar unificado para todas las pantallas
@Composable
fun LaSanteHeader(
    onBack: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp), // Padding consistente
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GreenNavButton(
            assetPath = "svg/ui/Before.svg",
            contentDescription = "Volver",
            onClick = onBack,
            size = buttonSize
        )
        
        // Espacio central para buscador o títulos específicos
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }

        GreenNavButton(
            assetPath = "svg/ui/Home.svg",
            contentDescription = "Inicio",
            onClick = onHome,
            size = buttonSize
        )
    }
}

@Composable
fun LaSanteScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 28,
    textAlign: TextAlign = TextAlign.Start,
    textBrush: Brush? = null,
    underlineBrush: Brush? = null,
    underlineWidth: Dp = 40.dp // DEFAULT SHORTER UNDERLINE WIDTH
) {
    // THIS COLUMN NOW WRAPS ITS CONTENT TO ALLOW PROPER CENTERING OF THE UNDERLINE
    Column(
        modifier = modifier.wrapContentSize(), // <--- KEY CHANGE HERE
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        val textStyle = if (textBrush != null) {
            MaterialTheme.typography.headlineMedium.copy(
                brush = textBrush,
                fontSize = fontSize.sp,
                letterSpacing = 1.sp,
                textAlign = textAlign,
            )
        } else {
            MaterialTheme.typography.headlineMedium.copy(
                color = LaSanteGreen,
                fontSize = fontSize.sp,
                letterSpacing = 1.sp,
                textAlign = textAlign,
            )
        }

        Text(
            text = text.uppercase(),
            style = textStyle,
            color = Color.Unspecified // Always Unspecified here if style is handled completely by TextStyle
        )
        if (underlineBrush != null) {
            Box(
                modifier = Modifier
                    .width(underlineWidth)
                    .height(2.dp)
                    .background(underlineBrush, RoundedCornerShape(1.dp))
                    .align(Alignment.CenterHorizontally) // Centrar la línea con respecto al texto (dentro de este Column que ahora es wrapContentSize)
            )
        }
    }
}

@Composable
fun RealGreenScrollBar(
    scrollFraction: Float,
    thumbFraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(8.dp)
            .fillMaxHeight()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackHeight = size.height
            val normalizedThumbFraction = thumbFraction.coerceIn(0.18f, 0.72f)
            val thumbHeight = (trackHeight * normalizedThumbFraction).coerceAtLeast(60f)
            val scrollableHeight = trackHeight - thumbHeight
            val thumbY = scrollFraction * scrollableHeight

            drawRoundRect(
                color = Color(0xFFD6D6D6),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(10f)
            )
            drawRoundRect(
                color = LaSanteGreen,
                topLeft = Offset(0f, thumbY),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(10f)
            )
        }
    }
}
