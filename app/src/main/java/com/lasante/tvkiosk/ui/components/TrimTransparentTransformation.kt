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
 */
class TrimTransparentTransformation(
    private val alphaThreshold: Int = 8,
    private val marginFraction: Float = 0.02f,
    private val makeSquare: Boolean = true,
) : Transformation {

    override val cacheKey: String =
        "trim_transparent_a${alphaThreshold}_m${marginFraction}_sq$makeSquare"

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
        val margin = max(2, (max(contentW, contentH) * marginFraction).toInt())
        val left = max(0, minX - margin)
        val top = max(0, minY - margin)
        val right = min(w, maxX + 1 + margin)
        val bottom = min(h, maxY + 1 + margin)
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
