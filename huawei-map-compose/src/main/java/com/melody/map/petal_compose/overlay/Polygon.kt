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
import com.huawei.hms.maps.model.GroundOverlay
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.PatternItem
import com.huawei.hms.maps.model.Polygon
import com.huawei.hms.maps.model.PolygonOptions
import com.melody.map.petal_compose.MapApplier
import com.melody.map.petal_compose.MapNode
import com.melody.map.petal_compose.model.HWMapComposable

internal class PolygonNode(
    val polygon: Polygon,
    val onPolygonClick: (Polygon) -> Unit
) : MapNode {
    override fun onRemoved() {
        polygon.remove()
    }
}

/**
 * 在地图上绘制多边形覆盖物。一个多边形可以凸面体，也可是凹面体。
 *
 * @param points 多边形的顶点坐标列表
 * @param patternItems 多边形的边框样式
 * @param fillColor 多边形的填充颜色
 * @param strokeColor 多边形的边框颜色
 * @param strokeWidth 多边形的边框宽度，单位：像素
 * @param isClickable 多边形是否可以点击。
 * @param visible 多边形的可见属性。当不可见时，多边形将不会被绘制，但是其他属性将会保存。
 * @param zIndex 多边形的显示层级
 */
@Composable
@HWMapComposable
fun Polygon(
    points: List<LatLng>,
    patternItems: List<PatternItem> = emptyList(),
    fillColor: Color = Color.Black,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    isClickable: Boolean = true,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onPolygonClick: (Polygon) -> Unit = {}
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val polygon = mapApplier?.map?.addPolygon(PolygonOptions().apply  {
                addAll(points)
                fillColor(fillColor.toArgb())
                strokeColor(strokeColor.toArgb())
                strokeWidth(strokeWidth)
                strokePattern(patternItems)
                clickable(isClickable)
                visible(visible)
                zIndex(zIndex)
            }) ?: error("Error adding polygon")
            PolygonNode(polygon,onPolygonClick)
        },
        update = {
            set(points) { this.polygon.points = it }
            set(patternItems) { this.polygon.strokePattern = it }
            set(isClickable) { this.polygon.isClickable = it }
            set(fillColor) { this.polygon.fillColor = it.toArgb() }
            set(strokeColor) { this.polygon.strokeColor = it.toArgb() }
            set(strokeWidth) { this.polygon.strokeWidth = it }
            set(visible) { this.polygon.isVisible = it }
            set(zIndex) { this.polygon.zIndex = it }
        }
    )
}
