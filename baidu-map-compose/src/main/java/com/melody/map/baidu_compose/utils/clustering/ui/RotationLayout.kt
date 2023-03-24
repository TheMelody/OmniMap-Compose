/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * RotationLayout rotates the contents of the layout by multiples of 90 degrees.
 *
 *
 * May not work with padding.
 */
internal class RotationLayout : FrameLayout {
    private var mRotation = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mRotation == 1 || mRotation == 3) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * @param degrees the rotation, in degrees.
     */
    fun setViewRotation(degrees: Int) {
        mRotation = (degrees + 360) % 360 / 90
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (mRotation == 0) {
            super.dispatchDraw(canvas)
            return
        }
        if (mRotation == 1) {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f, (width / 2).toFloat(), 0f)
            canvas.translate((height / 2).toFloat(), (width / 2).toFloat())
        } else if (mRotation == 2) {
            canvas.rotate(180f, (width / 2).toFloat(), (height / 2).toFloat())
        } else {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(270f, (width / 2).toFloat(), 0f)
            canvas.translate((height / 2).toFloat(), (-width / 2).toFloat())
        }
        super.dispatchDraw(canvas)
    }
}