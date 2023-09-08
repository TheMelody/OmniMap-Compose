// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
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

package com.melody.map.petal_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huawei.hms.maps.model.Circle
import com.huawei.hms.maps.model.CircleOptions
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.PatternItem
import com.huawei.hms.maps.model.Polygon
import com.melody.map.petal_compose.MapApplier
import com.melody.map.petal_compose.MapNode
import com.melody.map.petal_compose.model.HWMapComposable

internal class CircleNode(
    val circle: Circle,
    val onCircleClick: (Circle) -> Unit
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
 * @param patternItems 圆的边框样式
 * @param isClickable 圆形是否可以点击。
 * @param visible 圆是否可见
 * @param zIndex 设置显示的层级，越大越靠上显示
 */
@Composable
@HWMapComposable
fun Circle(
    center: LatLng,
    fillColor: Color = Color.Transparent,
    radius: Double = 0.0,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    patternItems: List<PatternItem> = emptyList(),
    isClickable: Boolean = true,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onCircleClick: (Circle) -> Unit = {}
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val circle = mapApplier?.map?.addCircle(
                CircleOptions().apply  {
                    center(center)
                    fillColor(fillColor.toArgb())
                    radius(radius)
                    strokePattern(patternItems)
                    strokeColor(strokeColor.toArgb())
                    strokeWidth(strokeWidth)
                    clickable(isClickable)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) ?: error("Error adding circle")
            CircleNode(circle,onCircleClick)
        },
        update = {
            set(center) { this.circle.center = it }
            set(fillColor) { this.circle.fillColor = it.toArgb() }
            set(isClickable) { this.circle.isClickable = it }
            set(radius) { this.circle.radius = it }
            set(strokeColor) { this.circle.strokeColor = it.toArgb() }
            set(strokeWidth) { this.circle.strokeWidth = it }
            set(patternItems) { this.circle.strokePattern = it }
            set(visible) { this.circle.isVisible = it }
            set(zIndex) { this.circle.zIndex = it }
        }
    )
}