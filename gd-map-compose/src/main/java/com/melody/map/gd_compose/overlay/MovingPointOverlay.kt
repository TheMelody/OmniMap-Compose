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
import androidx.compose.ui.geometry.Offset
import com.amap.api.maps.model.*
import com.amap.api.maps.utils.overlay.MovingPointOverlay
import com.melody.map.gd_compose.MapApplier
import com.melody.map.gd_compose.MapNode
import com.melody.map.gd_compose.model.GDMapComposable

internal class MovingPointOverlayNode(
    val marker: Marker,
    val movingPointOverlay: MovingPointOverlay,
    var onMarkerClick: (Marker) -> Boolean
) : MapNode {
    override fun onRemoved() {
        marker.remove()
        movingPointOverlay.removeMarker()
    }
}

/**
 * MovingPointOverlay
 * @param points   所有轨迹
 * @param descriptor 移动的Marker图标
 * @param isStartSmoothMove 是否开始移动
 * @param totalDuration 2点之间移动的总时长
 * @param anchor 移动的Marker的锚点位置
 * @param alpha 移动的Marker透明度
 * @param visible MovingPointOverlay的可见性
 * @param zIndex 移动的Marker的zIndex绘制层级
 * @param onClick 移动的Marker点击事件
 */
@Composable
@GDMapComposable
fun MovingPointOverlay(
    points: List<LatLng>,
    descriptor: BitmapDescriptor?,
    isStartSmoothMove: Boolean,
    totalDuration: Int,
    anchor: Offset = Offset(0.5F, 0.5F),
    alpha: Float = 1.0F,
    visible: Boolean = true,
    zIndex: Float = 0F,
    onClick: (Marker) -> Boolean = { false }
){
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<MovingPointOverlayNode, MapApplier>(
        factory = {
            val aMap = mapApplier?.map?: error("Error init MovingPointOverlay")
            val marker = aMap.addMarker(MarkerOptions().apply {
                icon(descriptor)
                zIndex(zIndex)
                alpha(alpha)
                anchor(anchor.x, anchor.y)
            })
            marker.`object` = points
            val overlay = MovingPointOverlay(aMap, marker)
            overlay.setPoints(points)
            overlay.setTotalDuration(totalDuration)
            overlay.setVisible(visible)
            MovingPointOverlayNode(
                marker = marker,
                onMarkerClick = onClick,
                movingPointOverlay = overlay
            )
        },
        update = {
            update(onClick) { this.onMarkerClick = it }

            set(alpha) { this.marker.alpha = it }
            set(anchor) { this.marker.setAnchor(it.x, it.y) }
            set(descriptor) { this.marker.setIcon(it) }
            set(points) { this.movingPointOverlay.setPoints(it) }
            set(totalDuration) { this.movingPointOverlay.setTotalDuration(it) }
            set(visible) { this.movingPointOverlay.setVisible(it) }
            set(isStartSmoothMove) {
                if(it) {
                    this.movingPointOverlay.startSmoothMove()
                } else {
                    this.movingPointOverlay.stopMove()
                }
            }
        }
    )
}