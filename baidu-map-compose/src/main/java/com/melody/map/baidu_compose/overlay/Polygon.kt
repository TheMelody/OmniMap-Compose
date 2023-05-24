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

package com.melody.map.baidu_compose.overlay

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.Polygon
import com.baidu.mapapi.map.PolygonHoleOptions
import com.baidu.mapapi.map.PolygonOptions
import com.baidu.mapapi.map.PolylineDottedLineType
import com.baidu.mapapi.map.PolylineDottedLineType.DOTTED_LINE_SQUARE
import com.baidu.mapapi.map.Stroke
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class PolygonNode(
    val polygon: Polygon
) : MapNode {
    override fun onRemoved() {
        polygon.remove()
    }
}

/**
 * 在地图上绘制多边形覆盖物。一个多边形可以凸面体，也可是凹面体。
 *
 * @param points 多边形的顶点坐标列表
 * @param holeOption (可选)，多边形添加空心洞效果
 * @param fillColor 多边形的填充颜色
 * @param strokeColor 多边形的边框颜色
 * @param strokeWidth 多边形的边框宽度，单位：像素
 * @param isDottedStroke (**不支持二次更新**)多边形是否绘制虚线边框
 * @param dottedStrokeType (**不支持二次更新**)多边形虚线边框类型
 * @param visible 多边形的可见属性。当不可见时，多边形将不会被绘制，但是其他属性将会保存。
 * @param tag 多边形覆盖物额外信息
 * @param zIndex 多边形的显示层级
 */
@Composable
@BDMapComposable
fun Polygon(
    points: List<LatLng>,
    holeOption: PolygonHoleOptions? = null,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Int = 10,
    isDottedStroke: Boolean = false,
    dottedStrokeType: PolylineDottedLineType = DOTTED_LINE_SQUARE,
    visible: Boolean = true,
    tag: Bundle? = null,
    zIndex: Int = 0
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val polygon = mapApplier?.map?.addOverlay(PolygonOptions().apply  {
                points(points)
                fillColor(fillColor.toArgb())
                stroke(Stroke(strokeWidth, strokeColor.toArgb()))
                holeOption?.let { addHoleOption(holeOption) }
                dottedStroke(isDottedStroke)
                dottedStrokeType(dottedStrokeType)
                extraInfo(tag)
                visible(visible)
                zIndex(zIndex)
            }) as? Polygon ?: error("Error adding polygon")
            PolygonNode(polygon)
        },
        update = {
            set(points) { this.polygon.points = it }
            set(holeOption) { this.polygon.holeOption = it }
            set(fillColor) { this.polygon.fillColor = it.toArgb() }
            set(strokeColor) { this.polygon.stroke = Stroke(strokeWidth, it.toArgb()) }
            set(strokeWidth) { this.polygon.stroke = Stroke(it, strokeColor.toArgb()) }
            set(visible) { this.polygon.isVisible = it }
            set(tag) { this.polygon.extraInfo = it }
            set(zIndex) { this.polygon.zIndex = it }
        }
    )
}
