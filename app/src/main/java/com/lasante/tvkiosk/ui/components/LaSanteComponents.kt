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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.ui.platform.LocalDensity
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
    textColor: Color? = null,
    underlineBrush: Brush? = null,
    underlineWidth: Dp = 40.dp, // fallback until text is measured
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    allCaps: Boolean = true,
    underlineMatchTextWidth: Boolean = false,
) {
    val density = LocalDensity.current
    var measuredUnderlineWidth by remember(text, fontSize, allCaps) { mutableStateOf(0.dp) }

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        val baseStyle = MaterialTheme.typography.headlineMedium.copy(
            fontSize = fontSize.sp,
            letterSpacing = 1.sp,
            textAlign = textAlign,
            fontFamily = fontFamily ?: MaterialTheme.typography.headlineMedium.fontFamily,
            fontWeight = fontWeight ?: MaterialTheme.typography.headlineMedium.fontWeight,
        )
        val textStyle = when {
            textBrush != null -> baseStyle.copy(brush = textBrush)
            textColor != null -> baseStyle.copy(color = textColor)
            else -> baseStyle.copy(color = LaSanteGreen)
        }

        Text(
            text = if (allCaps) text.uppercase() else text,
            style = textStyle,
            color = Color.Unspecified,
            onTextLayout = { layout ->
                if (underlineMatchTextWidth) {
                    measuredUnderlineWidth = with(density) { layout.size.width.toDp() }
                }
            },
        )
        if (underlineBrush != null) {
            val lineWidth = if (underlineMatchTextWidth && measuredUnderlineWidth > 0.dp) {
                measuredUnderlineWidth
            } else {
                underlineWidth
            }
            Box(
                modifier = Modifier
                    .width(lineWidth)
                    .height(2.dp)
                    .background(underlineBrush, RoundedCornerShape(1.dp))
                    .then(
                        if (textAlign == TextAlign.Center) {
                            Modifier.align(Alignment.CenterHorizontally)
                        } else {
                            Modifier.align(Alignment.Start)
                        },
                    ),
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
            // Sin tope artificial: si cabe casi todo, el thumb debe verse casi completo.
            val normalizedThumbFraction = thumbFraction.coerceIn(0.12f, 1f)
            val thumbHeight = (trackHeight * normalizedThumbFraction)
                .coerceIn(28f, trackHeight)
            val scrollableHeight = (trackHeight - thumbHeight).coerceAtLeast(0f)
            val thumbY = scrollFraction.coerceIn(0f, 1f) * scrollableHeight

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
