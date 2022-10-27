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
import com.tencent.tencentmap.mapsdk.maps.model.BaseAnimation
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Polyline
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions

internal class PolylineNode(
    val polyline: Polyline,
    var onPolylineClick: (Polyline) -> Unit
) : MapNode {
    override fun onRemoved() {
        polyline.setAnimation(null)
        polyline.remove()
    }
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * @param points 线段的坐标点列表
 * @param color 线段的颜色
 * @param visible 线段的可见属性
 * @param lineType 线段的类型，必须LineType里面的一种
 * @param useGradient 线段是否使用渐变色
 * @param isRoad 线段是否为路线
 * @param isLineCap 路线是否显示半圆端点
 * @param isClickable 是否可点击
 * @param animation 目前只有[com.tencent.tencentmap.mapsdk.maps.model.AlphaAnimation]、[com.amap.api.maps.model.animation.EmergeAnimation]支持
 * @param lineCustomTexture 线段的纹理图
 * @param tag 线段的附件对象
 * @param width 线段宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@TXMapComposable
fun Polyline(
    points: List<LatLng>,
    color: Color = Color.Black,
    visible: Boolean = true,
    useGradient: Boolean = false,
    isRoad: Boolean = false,
    isLineCap: Boolean = false,
    isClickable: Boolean = true,
    animation: BaseAnimation? = null,
    lineCustomTexture: BitmapDescriptor? = null,
    lineType : Int = PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE,
    tag: Any? = null,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {}
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addPolyline (
                PolylineOptions().apply {
                    addAll(points)
                    color(color.toArgb())
                    lineType(lineType)
                    aboveMaskLayer(isAboveMaskLayer)
                    colorTexture(lineCustomTexture)
                    gradient(useGradient)
                    road(isRoad)
                    lineCap(isLineCap)
                    animation(animation)
                    clickable(isClickable)
                    visible(visible)
                    width(width)
                }) ?: error("Error adding Polyline")
            polyline.tag = tag
            if(null != animation) {
                polyline.startAnimation(animation)
            }
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            set(points) { this.polyline.points = it }
            set(color) { this.polyline.color = it.toArgb() }
            set(tag) { this.polyline.tag = it }
            set(lineCustomTexture) { this.polyline.setColorTexture(it) }
            set(useGradient) { this.polyline.isGradientEnable = it }
            set(visible) { this.polyline.isVisible = it }
            set(isClickable) { this.polyline.isClickable = it }
            set(animation) {
                if(null != it) {
                    this.polyline.startAnimation(it)
                } else {
                    this.polyline.setAnimation(null)
                }
            }
            set(width) { this.polyline.width = it }
            set(zIndex) { this.polyline.setZIndex(it) }
        }
    )
}