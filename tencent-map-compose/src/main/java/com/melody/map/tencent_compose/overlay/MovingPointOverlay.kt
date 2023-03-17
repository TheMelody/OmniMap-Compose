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
import androidx.compose.ui.geometry.Offset
import com.melody.map.tencent_compose.MapApplier
import com.melody.map.tencent_compose.MapNode
import com.melody.map.tencent_compose.model.TXMapComposable
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions
import com.tencent.tencentmap.mapsdk.vector.utils.animation.MarkerTranslateAnimator

internal class MovingPointOverlayNode(
    val marker: Marker,
    val animator: MarkerTranslateAnimator,
    var onMarkerClick: (Marker) -> Boolean
) : MapNode {
    override fun onRemoved() {
        animator.cancelAnimation()
        marker.remove()
    }
}

/**
 * MovingPointOverlay
 * @param points   所有轨迹
 * @param icon 移动的Marker图标
 * @param isStartSmoothMove 是否开始移动
 * @param isFlat【初始化配置，**不支持二次更新**】Marker是否平贴在地图上
 * @param isClockwise【初始化配置，**不支持二次更新**】Marker旋转角度是否沿顺时针方向
 * @param totalDuration 移动的总时长
 * @param anchor 移动的Marker的锚点位置
 * @param alpha 移动的Marker透明度
 * @param visible MovingPointOverlay的可见性
 * @param zIndex 移动的Marker的zIndex绘制层级
 * @param onClick 移动的Marker点击事件
 */
@Composable
@TXMapComposable
fun MovingPointOverlay(
    points: List<LatLng>,
    icon: BitmapDescriptor?,
    isStartSmoothMove: Boolean,
    isFlat: Boolean = true,
    isClockwise: Boolean = false,
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
            if(points.isEmpty()){
                error("Error points size == 0")
            }
            val marker = aMap.addMarker(MarkerOptions(points[0]).apply {
                icon(icon)
                zIndex(zIndex)
                alpha(alpha)
                clockwise(isClockwise)
                flat(isFlat)
                visible(visible)
                anchor(anchor.x, anchor.y)
            })
            marker.tag = points
            val animator = MarkerTranslateAnimator(marker,totalDuration.toLong(), points.toTypedArray(), true)
            MovingPointOverlayNode(
                marker = marker,
                onMarkerClick = onClick,
                animator = animator
            )
        },
        update = {
            update(onClick) { this.onMarkerClick = it }

            set(alpha) { this.marker.alpha = it }
            set(anchor) { this.marker.setAnchor(it.x, it.y) }
            set(icon) { this.marker.setIcon(it) }
            set(visible) { this.marker.isVisible = it }
            // Marker.setMarkerOptions 已废弃
            //set(isFlat) { this.marker.setMarkerOptions(this.marker.options.flat(it)) }
            //set(isClockwise) { this.marker.setMarkerOptions(this.marker.options.clockwise(it)) }
            set(isStartSmoothMove) {
                //不支持暂停，只支持停止，其实也能调用暂停，就是点击恢复的时候，小车方向和角度不正确了！！！！
                if(it) {
                    this.animator.startAnimation()
                } else {
                    this.animator.endAnimation()
                }
            }
        }
    )
}