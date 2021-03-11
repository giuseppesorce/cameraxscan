package com.gs.scancamerax

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.media.Image
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mlkit.vision.barcode.Barcode


class BarcodeOverlay @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = -1
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var barcodes = listOf<Barcode>()
    private var scaleFactorX = 1.0f
    private var scaleFactorY = 1.0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            for (barcode in barcodes) {
                barcode.boundingBox?.let { boundingBox ->
                    val rect = translateRect(boundingBox)
                    val cx = rect.left + (rect.right - rect.left) / 2
                    val cy = rect.top + (rect.bottom - rect.top) / 2
                    val radius = 16.0f
//                    rect.bottom = rect.bottom + (rect.bottom -rect.top)/2
//                   rect.top = rect.top - (rect.bottom-rect.bottom)

                    Log.i("driver", "Rect: ${rect.toShortString()}")
                    drawRect(rect, paint)
                }
            }
        }
    }

    fun update(scanResult: Image, codes: List<Barcode>) {
        if (isPortraitMode()) {
            scaleFactorY = height.toFloat() / scanResult.width
            scaleFactorX = width.toFloat() / scanResult.height
        } else {
            scaleFactorY = height.toFloat() / scanResult.height
            scaleFactorX = width.toFloat() / scanResult.width
        }
        barcodes= codes
        invalidate()
    }

    private fun isPortraitMode(): Boolean {
        val orientation: Int = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun translateX(x: Float): Float = x * scaleFactorX
    private fun translateY(y: Float): Float = y * scaleFactorY

    private fun translateRect(rect: Rect) = RectF(
            translateX(rect.left.toFloat()),
            translateY(rect.top.toFloat()),
            translateX(rect.right.toFloat()),
            translateY(rect.bottom.toFloat())
    )
}
