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
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Polygon
import com.tencent.tencentmap.mapsdk.maps.model.PolygonOptions

internal class PolygonNode(
    val polygon: Polygon,
    var onClick: (Polygon) -> Unit
) : MapNode {
    override fun onRemoved() {
        polygon.remove()
    }
}

class PolygonBorder private constructor(
    val texture: BitmapDescriptor? = null,
    val textureSpacing: Int = 30,
    val patterns: List<Int>? = null
) {
    companion object {
        /**
         * @param textureSpacing 纹理之间的间隔，单位：像素
         * @param texture 纹理图片，这个纹理会重复地绘填充到线上，同时用户应配置纹理间隔 textureSpacing(int)， 但是它与 strokeColor(int)、pattern(List)两个配置互斥，这三个接口最后调用的会生效
         */
        fun create(textureSpacing: Int, texture: BitmapDescriptor) : PolygonBorder {
            return PolygonBorder(texture,textureSpacing)
        }

        /**
         * @param patterns 元素数量必须是偶数个，每对元素分别表示虚线中实线区域的长度，以及空白区域的长度（单位px)
         */
        fun create(patterns: List<Int>) : PolygonBorder {
            return PolygonBorder(patterns = patterns)
        }
    }
}

/**
 * 在地图上绘制多边形覆盖物。一个多边形可以凸面体，也可是凹面体。
 *
 * @param polygonBorder (可选)，多边形的边框有虚线和点两种方式
 * @param points 多边形的顶点坐标列表
 * @param fillColor 多边形的填充颜色
 * @param strokeColor 多边形的边框颜色
 * @param strokeWidth 多边形的边框宽度，单位：像素
 * @param tag 多边形的附加信息对象
 * @param visible 多边形的可见属性。当不可见时，多边形将不会被绘制，但是其他属性将会保存。
 * @param zIndex 多边形的显示层级
 * @param isClickable 多边形是否可点击
 * @param onClick 多边形点击事件回调
 */
@Composable
@TXMapComposable
fun Polygon(
    points: List<LatLng>,
    polygonBorder: PolygonBorder? = null,
    fillColor: Color = Color.Black,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 10f,
    tag: Any? = null,
    visible: Boolean = true,
    isClickable: Boolean = true,
    zIndex: Float = 0f,
    onClick: (Polygon) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            val polygon = mapApplier?.map?.addPolygon(PolygonOptions().apply {
                addAll(points)
                fillColor(fillColor.toArgb())
                strokeColor(strokeColor.toArgb())
                strokeWidth(strokeWidth)
                clickable(isClickable)
                customTexture(polygonBorder)
                visible(visible)
            }) ?: error("Error adding polygon")
            polygon.tag = tag
            PolygonNode(polygon, onClick)
        },
        update = {
            update(onClick) { this.onClick = it }

            set(points) { this.polygon.points = it }
            set(tag) { this.polygon.tag = it }
            set(fillColor) { this.polygon.fillColor = it.toArgb() }
            set(strokeColor) { this.polygon.strokeColor = it.toArgb() }
            set(strokeWidth) { this.polygon.strokeWidth = it }
            set(isClickable) { this.polygon.isClickable = it }
            set(visible) { this.polygon.isVisible = it }
            set(zIndex) { this.polygon.setZIndex(it) }
        }
    )
}

/**
 * ARGB虚线的样式，与 texture(BitmapDescriptor) 互斥，只能设置一个
 */
private fun PolygonOptions.customTexture(polygonBorder: PolygonBorder?) {
    if(null == polygonBorder) return
    if (polygonBorder.texture != null) {
        texture(polygonBorder.texture)
        textureSpacing(polygonBorder.textureSpacing)
        return
    }
    if (polygonBorder.patterns != null) {
        pattern(polygonBorder.patterns)
    }
}
