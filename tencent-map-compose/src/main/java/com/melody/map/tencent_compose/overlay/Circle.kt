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
import com.tencent.tencentmap.mapsdk.maps.model.Circle
import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

internal class CircleNode(
    val circle: Circle
) : MapNode {
    override fun onRemoved() {
        circle.remove()
    }
}

/**
 * 在地图上绘制圆的覆盖物.
 *
 * @param center 圆心经纬度坐标
 * @param fillColor 圆的填充颜色
 * @param radius 圆的半径，单位米.
 * @param strokeColor 圆的边框颜色
 * @param strokeWidth 圆的边框宽度
 * @param tag 圆的附加信息对象
 * @param visible 圆是否可见
 * @param isClickable 是否支持点击,默认不可点击
 * @param zIndex 设置显示的层级，越大越靠上显示
 */
@Composable
@TXMapComposable
fun Circle(
    center: LatLng,
    fillColor: Color = Color.Transparent,
    radius: Double = 0.0,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    tag: Any? = null,
    visible: Boolean = true,
    isClickable: Boolean = false,
    zIndex: Float = 0f,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val circle = mapApplier?.map?.addCircle(
                CircleOptions().apply  {
                    center(center)
                    fillColor(fillColor.toArgb())
                    radius(radius)
                    clickable(isClickable)
                    strokeColor(strokeColor.toArgb())
                    strokeWidth(strokeWidth)
                    visible(visible)
                }
            ) ?: error("Error adding circle")
            circle.tag = tag
            CircleNode(circle)
        },
        update = {
            set(center) { this.circle.center = it }
            set(fillColor) { this.circle.fillColor = it.toArgb() }
            set(radius) { this.circle.radius = it }
            set(tag) { this.circle.tag = it }
            set(isClickable) { this.circle.isClickable = it }
            set(strokeColor) { this.circle.strokeColor = it.toArgb() }
            set(strokeWidth) { this.circle.strokeWidth = it }
            set(visible) { this.circle.isVisible = it }
            set(zIndex) { this.circle.setZIndex(it) }
        }
    )
}