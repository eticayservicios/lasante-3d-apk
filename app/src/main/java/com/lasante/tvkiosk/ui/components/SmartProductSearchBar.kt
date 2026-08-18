package com.lasante.tvkiosk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.lasante.tvkiosk.data.Product
import com.lasante.tvkiosk.ui.theme.LaSanteText
import com.lasante.tvkiosk.ui.theme.LaSanteTextSecondary
import com.lasante.tvkiosk.ui.theme.LaSanteWhite
import com.lasante.tvkiosk.ui.utils.UiSound
import com.lasante.tvkiosk.ui.utils.clickableWithSound
import java.text.Normalizer
import java.util.Locale

@Immutable
data class ProductSearchBarMetrics(
    val width: Dp,
    val height: Dp,
    val iconSize: Dp,
    val fontSize: TextUnit,
    val dropdownMaxHeight: Dp = 280.dp,
    val suggestionImageSize: Dp = 28.dp,
    /** Filas visibles en el desplegable; el resto se ve con scroll. */
    val dropdownVisibleRows: Int = 10,
    val suggestionLimit: Int = 24,
    /**
     * Radio de esquina ~28 % del alto: semi-cuadrado con bordes redondeados.
     * `percent = 50` haría cápsula (extremos circulares); el mockup no lo es.
     */
    val cornerRadius: Dp = height * 0.28f,
    /** Bloque verde cuadrado (ancho = alto), no medio círculo. */
    val greenWidth: Dp = height,
) {
    companion object {
        /** Mismo tamaño en Intro y CT (TV66 ≈ 239×34). */
        fun kioskChrome(
            isTv66: Boolean,
            isTv42OrLarge: Boolean,
            isPhoneLandscape: Boolean,
            suggestionLimit: Int = 24,
            dropdownVisibleRows: Int = 10,
        ): ProductSearchBarMetrics {
            val width = when {
                isTv66 -> 228.dp
                isTv42OrLarge -> 196.dp
                isPhoneLandscape -> 168.dp
                else -> 184.dp
            } * 1.05f
            val height = when {
                isTv66 -> 32.dp
                isTv42OrLarge -> 28.dp
                isPhoneLandscape -> 24.dp
                else -> 26.dp
            } * 1.05f
            val iconSize = when {
                isTv66 -> 16.dp
                isTv42OrLarge -> 14.dp
                else -> 13.dp
            } * 1.05f
            val fontSize = when {
                isTv66 -> 12.sp
                isTv42OrLarge -> 11.sp
                else -> 10.sp
            }
            return ProductSearchBarMetrics(
                width = width,
                height = height,
                iconSize = iconSize,
                fontSize = fontSize,
                suggestionLimit = suggestionLimit,
                dropdownVisibleRows = dropdownVisibleRows,
            )
        }
    }
}

private const val DEFAULT_PLACEHOLDER = "¿Buscas algun producto?"
private val SearchBarGreen = Color(0xFF68BD45)
private val SuggestionRowHeight = 40.dp
private val SuggestionListPadding = 4.dp

/**
 * Buscador inteligente: coincide solo en **nombre** (y dosis como fallback), nunca en descripción.
 * Prioridad: empieza por la búsqueda → palabra que empieza por → contiene en cualquier parte del nombre.
 */
@Composable
fun SmartProductSearchBar(
    products: List<Product>,
    onProductSelected: (Product) -> Unit,
    metrics: ProductSearchBarMetrics,
    modifier: Modifier = Modifier,
    placeholder: String = DEFAULT_PLACEHOLDER,
    enabled: Boolean = true,
    /** Si la pantalla permanece montada al navegar (Intro), pasar false al salir para vaciar la búsqueda. */
    screenActive: Boolean = true,
    onInteraction: () -> Unit = {},
    /** true con teclado o lista abiertos; solo cambia al abrir/cerrar, no en cada letra. */
    onActiveChange: (Boolean) -> Unit = {},
    /** true mientras el campo tiene foco / teclado. */
    onEditingChange: (Boolean) -> Unit = {},
    /** Incrementar para cerrar la lista (segundo toque fuera). */
    dismissListTick: Int = 0,
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var focused by remember { mutableStateOf(false) }
    var pendingFocus by remember { mutableStateOf(false) }
    /** Mantiene la lista visible tras abrir un producto (desenfoque del modal). */
    var pinResultsOpen by remember { mutableStateOf(false) }
    var retainResultsOnBlur by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val queryText = query.text
    val suggestions = remember(queryText, products) {
        rankSmartProductMatches(products, queryText, metrics.suggestionLimit)
    }
    val showList = queryText.isNotBlank() && (focused || pinResultsOpen)
    val showDropdown = showList && suggestions.isNotEmpty()
    val showEmpty = showList && suggestions.isEmpty()
    val barShape = RoundedCornerShape(metrics.cornerRadius)
    val greenShape = RoundedCornerShape(metrics.cornerRadius)

    fun moveCursorToEnd() {
        val end = query.text.length
        if (query.selection.start != end || query.selection.end != end) {
            query = query.copy(selection = TextRange(end))
        }
    }

    fun hideKeyboardKeepList() {
        if (query.text.isNotBlank()) {
            pinResultsOpen = true
        }
        pendingFocus = false
        focusManager.clearFocus()
        keyboard?.hide()
    }

    fun focusSearchField() {
        if (!enabled) return
        onInteraction()
        moveCursorToEnd()
        if (focused) {
            keyboard?.show()
        } else {
            pendingFocus = true
        }
    }

    fun closeEditing(clearQuery: Boolean) {
        if (clearQuery) {
            query = TextFieldValue("")
            pinResultsOpen = false
        }
        retainResultsOnBlur = false
        pendingFocus = false
        focused = false
        focusManager.clearFocus()
        keyboard?.hide()
    }

    LaunchedEffect(pendingFocus, enabled) {
        if (pendingFocus && enabled) {
            focusRequester.requestFocus()
            keyboard?.show()
            pendingFocus = false
        }
    }

    LaunchedEffect(screenActive) {
        if (!screenActive) {
            closeEditing(clearQuery = true)
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) {
            pendingFocus = false
            focusManager.clearFocus()
            keyboard?.hide()
        }
    }

    fun openProduct(product: Product) {
        pinResultsOpen = query.text.isNotBlank()
        retainResultsOnBlur = true
        pendingFocus = false
        focusManager.clearFocus()
        keyboard?.hide()
        onProductSelected(product)
    }

    val searchSessionOpen = focused || showList
    val onActiveChangeState = rememberUpdatedState(onActiveChange)
    val onEditingChangeState = rememberUpdatedState(onEditingChange)
    LaunchedEffect(searchSessionOpen) {
        onActiveChangeState.value(searchSessionOpen)
    }
    LaunchedEffect(focused) {
        onEditingChangeState.value(focused)
    }
    LaunchedEffect(dismissListTick) {
        if (dismissListTick > 0) {
            pinResultsOpen = false
            retainResultsOnBlur = false
        }
    }

    Box(
        modifier = modifier
            .width(metrics.width)
            .height(metrics.height)
            .wrapContentHeight(unbounded = true, align = Alignment.Top)
            .zIndex(8f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(metrics.height)
                .shadow(elevation = 2.dp, shape = barShape)
                .clip(barShape)
                .background(LaSanteWhite),
        ) {
            val fieldTextStyle = TextStyle(
                color = LaSanteText,
                fontSize = metrics.fontSize,
                fontWeight = FontWeight.Light,
                lineHeight = metrics.fontSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxSize()
                    .padding(start = metrics.greenWidth + 8.dp, end = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { value ->
                        query = value
                        if (value.text.isBlank()) {
                            pinResultsOpen = false
                        }
                    },
                    enabled = enabled,
                    singleLine = true,
                    textStyle = fieldTextStyle,
                    cursorBrush = SolidColor(SearchBarGreen),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { hideKeyboardKeepList() },
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            val nowFocused = state.isFocused
                            focused = nowFocused
                            if (nowFocused) {
                                onInteraction()
                            } else {
                                keyboard?.hide()
                                if (query.text.isNotBlank()) {
                                    pinResultsOpen = true
                                }
                                retainResultsOnBlur = false
                            }
                        },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color(0xFFB0B0B0),
                                    fontSize = metrics.fontSize,
                                    fontWeight = FontWeight.Light,
                                    lineHeight = metrics.fontSize,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                                        lineHeightStyle = LineHeightStyle(
                                            alignment = LineHeightStyle.Alignment.Center,
                                            trim = LineHeightStyle.Trim.Both,
                                        ),
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(metrics.greenWidth)
                    .fillMaxHeight()
                    .clip(greenShape)
                    .background(SearchBarGreen)
                    .clickableWithSound(enabled = enabled) { focusSearchField() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar producto",
                    tint = Color.White,
                    modifier = Modifier.size(metrics.iconSize),
                )
            }
        }

        if (showDropdown || showEmpty) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = metrics.height + 6.dp)
                    .width(metrics.width)
                    .zIndex(9f),
            ) {
                if (showDropdown) {
                    SuggestionDropdown(
                        suggestions = suggestions,
                        metrics = metrics,
                        onSelect = ::openProduct,
                    )
                } else {
                    EmptySuggestionsCard(fontSize = metrics.fontSize)
                }
            }
        }
    }
}

@Composable
private fun EmptySuggestionsCard(fontSize: TextUnit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = shape)
            .clip(shape)
            .background(LaSanteWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = "Sin coincidencias",
            color = LaSanteTextSecondary,
            fontSize = fontSize,
        )
    }
}

@Composable
private fun SuggestionDropdown(
    suggestions: List<Product>,
    metrics: ProductSearchBarMetrics,
    onSelect: (Product) -> Unit,
) {
    val listState = rememberLazyListState()
    val scrollInfo by remember {
        derivedStateOf { computeLazyListScrollbar(listState) }
    }
    val visibleRows = metrics.dropdownVisibleRows.coerceAtLeast(1)
    val viewportRows = suggestions.size.coerceAtMost(visibleRows).coerceAtLeast(1)
    val dropdownHeight = SuggestionRowHeight * viewportRows + SuggestionListPadding * 2
    val showScrollbar = suggestions.size > visibleRows
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dropdownHeight)
            .shadow(elevation = 6.dp, shape = shape)
            .clip(shape)
            .background(LaSanteWhite)
            .border(1.dp, Color(0x14000000), shape),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(dropdownHeight)
                .padding(end = if (showScrollbar) 12.dp else 0.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = SuggestionListPadding),
            userScrollEnabled = true,
        ) {
            items(
                items = suggestions,
                key = { it.productoId },
            ) { product ->
                SuggestionRow(
                    product = product,
                    imageSize = metrics.suggestionImageSize,
                    titleSize = metrics.fontSize,
                    rowHeight = SuggestionRowHeight,
                    onClick = { onSelect(product) },
                )
            }
        }
        if (showScrollbar) {
            RealGreenScrollBar(
                scrollFraction = scrollInfo.scrollFraction,
                thumbFraction = scrollInfo.thumbFraction,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = 8.dp, bottom = 8.dp)
                    .height(dropdownHeight - 16.dp),
            )
        }
    }
}

private data class LazyListScrollbar(
    val canScroll: Boolean,
    val scrollFraction: Float,
    val thumbFraction: Float,
)

private fun computeLazyListScrollbar(state: LazyListState): LazyListScrollbar {
    val info = state.layoutInfo
    val visible = info.visibleItemsInfo
    if (info.totalItemsCount == 0 || visible.isEmpty()) {
        return LazyListScrollbar(canScroll = false, scrollFraction = 0f, thumbFraction = 1f)
    }

    val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat().coerceAtLeast(1f)
    val avgItemHeight = visible.map { it.size }.average().toFloat().coerceAtLeast(1f)
    val contentPadding = (info.beforeContentPadding + info.afterContentPadding).toFloat()
    val estimatedContent = info.totalItemsCount * avgItemHeight + contentPadding
    val maxScroll = (estimatedContent - viewport).coerceAtLeast(0f)
    val scrolled = state.firstVisibleItemIndex * avgItemHeight +
        state.firstVisibleItemScrollOffset.toFloat() +
        info.beforeContentPadding.toFloat()
    val canScroll = maxScroll > 2f || state.canScrollForward || state.canScrollBackward
    val scrollFraction = if (maxScroll > 0f) {
        (scrolled / maxScroll).coerceIn(0f, 1f)
    } else {
        0f
    }
    val thumbFraction = if (estimatedContent > 0f) {
        (viewport / estimatedContent).coerceIn(0.12f, 1f)
    } else {
        1f
    }
    return LazyListScrollbar(
        canScroll = canScroll,
        scrollFraction = scrollFraction,
        thumbFraction = thumbFraction,
    )
}

@Composable
private fun SuggestionRow(
    product: Product,
    imageSize: Dp,
    titleSize: TextUnit,
    rowHeight: Dp,
    onClick: () -> Unit,
) {
    val imageUrl = product.media.imagenes2d.miniatura?.trim()?.takeIf { it.isNotBlank() }
        ?: product.media.imagenes2d.principal?.trim()?.takeIf { it.isNotBlank() }
    val line = remember(product.nombre, product.dosisDisplay, product.formaFarmaceutica) {
        product.searchSuggestionLine()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickableWithSound(sound = UiSound.Product, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(imageSize)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF3F3F3)),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(imageSize),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Text(
            text = line,
            color = LaSanteText,
            fontSize = titleSize,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Una línea: "Título - dosis presentación". */
private fun Product.searchSuggestionLine(): String {
    val title = nombre.trim()
    val dose = dosisDisplay?.trim().orEmpty()
    val forma = when (formaFarmaceutica?.trim()?.lowercase()) {
        "comprimidos" -> "Comprimidos"
        "capsulas" -> "Cápsulas"
        "suspension" -> "Suspensión"
        else -> ""
    }
    val details = listOf(dose, forma).filter { it.isNotEmpty() }.joinToString(" ")
    return if (details.isEmpty()) title else "$title - $details"
}

internal fun rankSmartProductMatches(
    products: List<Product>,
    query: String,
    limit: Int,
): List<Product> {
    val normalizedQuery = query.normalizeForSearch()
    if (normalizedQuery.isEmpty()) return emptyList()
    return products.asSequence()
        .filter { it.nombre.isNotBlank() }
        .mapNotNull { product ->
            val name = product.nombre.normalizeForSearch()
            val dose = product.dosisDisplay?.normalizeForSearch().orEmpty()
            val nameWords = name.split(NAME_WORD_DELIMITER)
            val score = when {
                name.startsWith(normalizedQuery) -> 0
                nameWords.any { it.startsWith(normalizedQuery) } -> 1
                name.contains(normalizedQuery) -> 2
                normalizedQuery.length >= 2 &&
                    (dose.startsWith(normalizedQuery) || dose.contains(normalizedQuery)) -> 3
                else -> return@mapNotNull null
            }
            Triple(score, name, product)
        }
        .sortedWith(compareBy({ it.first }, { it.second }))
        .map { it.third }
        .distinctBy { it.productoId }
        .take(limit)
        .toList()
}

private val NAME_WORD_DELIMITER = Regex("[\\s-]+")

private fun String.normalizeForSearch(): String =
    Normalizer.normalize(this.trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
