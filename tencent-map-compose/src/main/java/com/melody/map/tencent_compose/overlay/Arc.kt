// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.map.tencent_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.maps.model.Arc
import com.tencent.tencentmap.mapsdk.maps.model.ArcOptions
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

internal class ArcNode(
    val arc: Arc
) : MapNode {
    override fun onRemoved() {
        arc.remove()
    }
}

/**
 * 弧线覆盖物
 * @param startPoint 起点位置坐标
 * @param endPoint 终点位置坐标
 * @param passedPoint 途经点位置坐标
 * @param strokeColor 弧形的边框颜色
 * @param strokeWidth 弧形的边框宽度，单位：像素
 * @param angle 设置起点到终点，与起点外切线逆时针旋转的夹角角度 通过设置起终点+夹角角度，即可确定一个圆弧线，如果同时设置途经点和夹角时，优先以夹角角度为准
 * @param showArrow 箭头显示状态，默认为false，不显示
 * @param visible 弧形是否可见
 * @param zIndex 弧形Z轴数值
 */
@Composable
@TXMapComposable
fun Arc(
    startPoint: LatLng,
    passedPoint: LatLng,
    endPoint: LatLng,
    color: Color = Color.Black,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    angle: Float = 0f,
    showArrow: Boolean = false,
    visible: Boolean = true,
    zIndex: Float = 0f,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<ArcNode, MapApplier>(
        factory = {
            val arc = mapApplier?.map?.addArc(
                ArcOptions().apply  {
                    points(startPoint,endPoint)
                    pass(passedPoint)
                    angle(angle)
                    color(color.toArgb())
                    showArrow(showArrow)
                    strokeColor(strokeColor.toArgb())
                    strokeWidth(strokeWidth)
                }
            ) ?: error("Error adding arc")
            arc.setZIndex(zIndex)
            arc.isVisible = visible
            ArcNode(arc)
        },
        update = {
            set(strokeColor) { this.arc.strokeColor = it.toArgb() }
            set(strokeWidth) { this.arc.strokeWidth = it }
            set(visible) { this.arc.isVisible = it }
            set(color) { this.arc.color = it.toArgb() }
            set(zIndex) { this.arc.setZIndex(it) }
        }
    )
}