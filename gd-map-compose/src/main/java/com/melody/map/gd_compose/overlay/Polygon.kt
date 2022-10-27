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

package com.melody.map.gd_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.model.AMapPara
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.amap.api.maps.model.AMapPara.LineJoinType
import com.amap.api.maps.model.BaseHoleOptions
import com.melody.map.gd_compose.model.GDMapComposable

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
 * @param holeOptions (可选)，多边形添加空心洞效果(目前来说效果不咋滴)，关键坐标点数据可通过DistrictSearch.searchDistrictAsync查询获取
 * @param fillColor 多边形的填充颜色
 * @param strokeColor 多边形的边框颜色
 * @param strokeWidth 多边形的边框宽度，单位：像素
 * @param visible 多边形的可见属性。当不可见时，多边形将不会被绘制，但是其他属性将会保存。
 * @param zIndex 多边形的显示层级
 * @param lineJoinType 边框连接处形状
 */
@Composable
@GDMapComposable
fun Polygon(
    points: List<LatLng>,
    holeOptions: List<BaseHoleOptions>? = null,
    fillColor: Color = Color.Black,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    visible: Boolean = true,
    zIndex: Float = 0f,
    lineJoinType: LineJoinType = LineJoinType.LineJoinBevel
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val polygon = mapApplier?.map?.addPolygon(PolygonOptions().apply  {
                addAll(points)
                fillColor(fillColor.toArgb())
                strokeColor(strokeColor.toArgb())
                strokeWidth(strokeWidth)
                lineJoinType(lineJoinType)
                setHoleOptions(holeOptions)
                visible(visible)
                zIndex(zIndex)
            }) ?: error("Error adding polygon")
            PolygonNode(polygon)
        },
        update = {
            set(points) { this.polygon.points = it }
            set(holeOptions) { this.polygon.holeOptions = it }
            set(fillColor) { this.polygon.fillColor = it.toArgb() }
            set(strokeColor) { this.polygon.strokeColor = it.toArgb() }
            set(strokeWidth) { this.polygon.strokeWidth = it }
            set(visible) { this.polygon.isVisible = it }
            set(zIndex) { this.polygon.zIndex = it }
        }
    )
}
