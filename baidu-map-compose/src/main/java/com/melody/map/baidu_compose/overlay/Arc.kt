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

package com.melody.map.baidu_compose.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.Arc
import com.baidu.mapapi.map.ArcOptions
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

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
 * @param visible 弧形是否可见
 * @param zIndex 弧形Z轴数值
 */
@Composable
@BDMapComposable
fun Arc(
    startPoint: LatLng,
    passedPoint: LatLng,
    endPoint: LatLng,
    strokeColor: Color = Color.Black,
    strokeWidth: Int = 10,
    visible: Boolean = true,
    zIndex: Int = 0,
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<ArcNode, MapApplier>(
        factory = {
            val arc = mapApplier?.map?.addOverlay(
                ArcOptions().apply  {
                    points(startPoint,passedPoint,endPoint)
                    color(strokeColor.toArgb())
                    width(strokeWidth)
                    visible(visible)
                    zIndex(zIndex)
                }
            ) as? Arc ?: error("Error adding arc")
            ArcNode(arc)
        },
        update = {
            set(startPoint) { this.arc.setPoints(it, passedPoint, endPoint) }
            set(passedPoint) { this.arc.setPoints(startPoint, it, endPoint) }
            set(endPoint) { this.arc.setPoints(startPoint, passedPoint, it) }
            set(strokeColor) { this.arc.color = it.toArgb() }
            set(strokeWidth) { this.arc.width = it }
            set(visible) { this.arc.isVisible = it }
            set(zIndex) { this.arc.zIndex = it }
        }
    )
}