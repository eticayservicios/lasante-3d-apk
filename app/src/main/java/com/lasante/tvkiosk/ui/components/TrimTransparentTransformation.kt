package com.lasante.tvkiosk.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.max
import kotlin.math.min

/**
 * Recorta padding transparente de iconos PNG del CDN (CloudFront).
 * Muchos assets 1080×1080 solo usan ~25–55% del canvas.
 *
 * [alphaThreshold] bajo (1–2) incluye anti-alias del arte; 8+ cortaba bordes en Hikvision.
 * [edgeExpandFraction] expande el recorte tras detectar bounds (margen de seguridad).
 */
class TrimTransparentTransformation(
    private val alphaThreshold: Int = 1,
    private val marginFraction: Float = 0.06f,
    private val edgeExpandFraction: Float = 0.05f,
    private val makeSquare: Boolean = true,
) : Transformation {

    override val cacheKey: String =
        "trim_v2_a${alphaThreshold}_m${marginFraction}_e${edgeExpandFraction}_sq$makeSquare"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val w = input.width
        val h = input.height
        if (w <= 0 || h <= 0) return input

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        val pixels = IntArray(w * h)
        input.getPixels(pixels, 0, w, 0, 0, w, h)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                val a = Color.alpha(pixels[row + x])
                if (a > alphaThreshold) {
                    if (x < minX) minX = x
                    if (y < minY) minY = y
                    if (x > maxX) maxX = x
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX || maxY < minY) return input

        val contentW = maxX - minX + 1
        val contentH = maxY - minY + 1
        val margin = max(4, (max(contentW, contentH) * marginFraction).toInt())
        val edgeExpand = max(4, (max(contentW, contentH) * edgeExpandFraction).toInt())
        val left = max(0, minX - margin - edgeExpand)
        val top = max(0, minY - margin - edgeExpand)
        val right = min(w, maxX + 1 + margin + edgeExpand)
        val bottom = min(h, maxY + 1 + margin + edgeExpand)
        val cropped = Bitmap.createBitmap(input, left, top, right - left, bottom - top)
        if (!makeSquare) return cropped

        val side = max(cropped.width, cropped.height)
        if (cropped.width == side && cropped.height == side) return cropped
        val square = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(square)
        canvas.drawBitmap(
            cropped,
            ((side - cropped.width) / 2).toFloat(),
            ((side - cropped.height) / 2).toFloat(),
            null,
        )
        if (cropped !== input) cropped.recycle()
        return square
    }
}
