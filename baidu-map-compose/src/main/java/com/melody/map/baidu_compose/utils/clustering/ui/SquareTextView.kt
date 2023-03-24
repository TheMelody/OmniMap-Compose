/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView

internal class SquareTextView : TextView {
    private var mOffsetTop = 0
    private var mOffsetLeft = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val dimension = width.coerceAtLeast(height)
        if (width > height) {
            mOffsetTop = width - height
            mOffsetLeft = 0
        } else {
            mOffsetTop = 0
            mOffsetLeft = height - width
        }
        setMeasuredDimension(dimension, dimension)
    }

    override fun draw(canvas: Canvas) {
        canvas.translate((mOffsetLeft / 2).toFloat(), (mOffsetTop / 2).toFloat())
        super.draw(canvas)
    }
}