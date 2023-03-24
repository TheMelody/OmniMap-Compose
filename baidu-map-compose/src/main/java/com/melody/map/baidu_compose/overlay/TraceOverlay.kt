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
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.track.TraceAnimationListener
import com.baidu.mapapi.map.track.TraceOptions
import com.baidu.mapapi.map.track.TraceOptions.TraceAnimateType
import com.baidu.mapapi.map.track.TraceOptions.TraceAnimateType.TraceOverlayAnimationEasingCurveLinear
import com.baidu.mapapi.map.track.TraceOverlay
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

private class TraceOverlayNode(
    var traceOverlay: TraceOverlay
) : MapNode {
    override fun onRemoved() {
        // 清除轨迹数据，但不会移除轨迹覆盖物
        traceOverlay.clear()
        // 移除轨迹覆盖物
        traceOverlay.remove()
    }
}

/**
 * 动态轨迹覆盖物
 * @param width 轨迹覆盖物宽度
 * @param color 轨迹覆盖物颜色
 * @param duration 轨迹覆盖物动画时长
 * @param points 轨迹覆盖物配置的经纬度点
 * @param isAnimate 轨迹覆盖物是否做动画
 * @param isPointMove 轨迹动画图标是否平滑移动
 * @param isTrackMove 地图是否跟轨迹一起运动，**默认：false**
 * @param icon 轨迹覆盖物图标
 * @param isRotateWhenTrack 轨迹覆盖物图标是否自动旋转角度，**默认：true**
 * @param animationType 动画类型
 */
@Composable
@BDMapComposable
fun TraceOverlay(
    width: Int,
    color: Color,
    duration: Int,
    points: List<LatLng>,
    isAnimate: Boolean,
    isPointMove: Boolean,
    isTrackMove: Boolean = false,
    icon: BitmapDescriptor? = null,
    isRotateWhenTrack: Boolean = true,
    animationType: TraceAnimateType = TraceOverlayAnimationEasingCurveLinear
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<TraceOverlayNode, MapApplier>(
        factory = {
            val listener = object : TraceAnimationListener {
                override fun onTraceAnimationUpdate(p0: Int) {
                }
                override fun onTraceUpdatePosition(p0: LatLng?) {
                }
                override fun onTraceAnimationFinish() {
                }
            }
            val traceOverlay = mapApplier?.map?.addTraceOverlay(
                TraceOptions().apply {
                    animationTime(duration)
                    color(color.toArgb())
                    points(points)
                    width(width)
                    animate(isAnimate)
                    setTrackMove(isTrackMove)
                    icon?.let { icon(it) }
                    setPointMove(isPointMove)
                    setRotateWhenTrack(isRotateWhenTrack)
                    animationType(animationType)
                },listener
            ) ?: error("Error adding TraceOverlay")
            TraceOverlayNode(traceOverlay)
        },
        update = {
            set(width) { this.traceOverlay.width = it }
            set(isAnimate) { this.traceOverlay.isAnimate = it }
            set(points) { this.traceOverlay.setTracePoints(it) }
            set(color) { this.traceOverlay.color = it.toArgb() }
            set(duration) { this.traceOverlay.animationTime = it }
            set(icon) {
                if(null != it) {
                    this.traceOverlay.icon(it)
                }
            }
            set(isPointMove) { this.traceOverlay.isPointMove = it }
            set(isTrackMove) { this.traceOverlay.isTrackMove = it }
            set(animationType) { this.traceOverlay.setTraceAnimationType(it) }
            set(isRotateWhenTrack) { this.traceOverlay.isRotateWhenTrack = it }
        }
    )
}