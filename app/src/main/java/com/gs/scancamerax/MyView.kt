package com.gs.scancamerax

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class MyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
): View(context, attrs, defStyleAttr) {

    private var rectangle: Rect = Rect(0,0,0,0)

    fun setBounds(bounds: Rect){
        rectangle.left = bounds.left
        rectangle.right = bounds.right
        rectangle.top = bounds.top
        rectangle.bottom = bounds.bottom
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawRect(rectangle, paint)
    }

}