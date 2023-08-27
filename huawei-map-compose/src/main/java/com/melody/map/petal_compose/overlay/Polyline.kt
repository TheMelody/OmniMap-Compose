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
import com.huawei.hms.maps.model.*
import com.melody.map.petal_compose.MapApplier
import com.melody.map.petal_compose.MapNode
import com.melody.map.petal_compose.model.HWMapComposable

internal class PolylineNode(
    val polyline: Polyline,
    var onPolylineClick: (Polyline) -> Unit
) : MapNode {
    override fun onRemoved() {
        polyline.remove()
    }
}

/**
 * 线的分段颜色（彩虹线）配置
 */
class PolylineRainbow private constructor(
    val colors: List<Int>
) {
    companion object {
        /**
         * 线的分段颜色（彩虹线）配置
         * @param colors 每段索引之间的颜色，这个颜色同样支持纹理颜色
         */
        fun create(colors: List<Int>): PolylineRainbow {
            if(colors.isEmpty()) error("Error colors.size == 0")
            return PolylineRainbow(colors)
        }
    }
}

/**
 * 普通的地图线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * @param points 线段的坐标点列表
 * @param polylineColor 线段的颜色
 * @param geodesic 线段是否画大地曲线，默认false，不画大地曲线
 * @param useGradient 线段是否为渐变的彩虹线段【默认为true】，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param visible 线段的可见属性
 * @param clickable 线段的是否可点击
 * @param startCap 折线的起始顶点，支持设置：[ButtCap]、[CustomCap]、[RoundCap]、[SquareCap]
 * @param endCap 折线的末端顶点，支持设置：[ButtCap]、[CustomCap]、[RoundCap]、[SquareCap]
 * @param patternItems 线段的样式
 * @param width 线段的宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@HWMapComposable
fun Polyline(
    points: List<LatLng>,
    polylineColor: Color = Color.Black,
    geodesic: Boolean = false,
    useGradient: Boolean = true,
    visible: Boolean = true,
    clickable: Boolean = true,
    startCap: Cap = ButtCap(),
    endCap: Cap = ButtCap(),
    tag: Any? = null,
    patternItems: List<PatternItem> = emptyList(),
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {}
) {
    PolylineImpl(
        points = points,
        rainbow = null,
        patternItems = patternItems,
        polylineColor = polylineColor,
        geodesic = geodesic,
        clickable = clickable,
        startCap = startCap,
        endCap = endCap,
        visible = visible,
        useGradient = useGradient,
        tag = tag,
        width = width,
        zIndex = zIndex,
        onClick = onClick
    )
}


/**
 * 彩虹线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * @param points 线段的坐标点列表
 * @param rainbow 线的分段颜色（彩虹线）
 * @param geodesic 线段是否画大地曲线，默认false，不画大地曲线
 * @param useGradient 线段是否为渐变的彩虹线段【默认为true】，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param visible 线段的可见属性
 * @param clickable 线段的是否可点击
 * @param startCap 折线的起始顶点，支持设置：[ButtCap]、[CustomCap]、[RoundCap]、[SquareCap]
 * @param endCap 折线的末端顶点，支持设置：[ButtCap]、[CustomCap]、[RoundCap]、[SquareCap]
 * @param patternItems 线段的样式
 * @param width 线段的宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@HWMapComposable
fun PolylineRainbow(
    points: List<LatLng>,
    patternItems: List<PatternItem>,
    rainbow: PolylineRainbow,
    useGradient: Boolean,
    geodesic: Boolean = false,
    visible: Boolean = true,
    clickable: Boolean = true,
    startCap: Cap = ButtCap(),
    endCap: Cap = ButtCap(),
    tag: Any? = null,
    width: Float = 10F,
    zIndex: Float = 0F,
    onClick: (Polyline) -> Unit
) {
    PolylineImpl(
        points = points,
        rainbow = rainbow,
        patternItems = patternItems,
        polylineColor = null,
        geodesic = geodesic,
        clickable = clickable,
        startCap = startCap,
        endCap = endCap,
        visible = visible,
        useGradient = useGradient,
        tag = tag,
        width = width,
        zIndex = zIndex,
        onClick = onClick
    )
}

@Composable
@HWMapComposable
private fun PolylineImpl(
    points: List<LatLng>,
    patternItems: List<PatternItem>,
    polylineColor: Color?,
    rainbow: PolylineRainbow?,
    geodesic: Boolean,
    useGradient: Boolean,
    visible: Boolean,
    clickable: Boolean,
    startCap: Cap,
    endCap: Cap,
    tag: Any?,
    width: Float,
    zIndex: Float,
    onClick: (Polyline) -> Unit
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addPolyline (
                PolylineOptions().apply {
                    addAll(points)
                    polylineColor?.let { color(polylineColor.toArgb()) }
                    geodesic(geodesic)
                    pattern(patternItems)
                    startCap(startCap)
                    endCap(endCap)
                    visible(visible)
                    clickable(clickable)
                    gradient(useGradient)
                    width(width)
                    zIndex(zIndex)
                }) ?: error("Error adding Polyline")
            tag?.let { polyline.tag = tag }
            polyline.rainbowColorLine(rainbow)
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            set(points) { this.polyline.points = it }
            set(polylineColor) { it?.let { this.polyline.color = it.toArgb() } }
            set(useGradient) { this.polyline.isGradient = it }
            set(geodesic) { this.polyline.isGeodesic = it }
            set(patternItems) { this.polyline.pattern = it }
            set(visible) { this.polyline.isVisible = it }
            set(startCap) { this.polyline.startCap = it }
            set(endCap) { this.polyline.endCap = it }
            set(tag) { it?.let { this.polyline.tag = it } }
            set(width) { this.polyline.width = it }
            set(zIndex) { this.polyline.zIndex = it }
        }
    )
}

/**
 * 设置彩虹线
 */
private fun Polyline.rainbowColorLine(polylineRainbow: PolylineRainbow?) {
    if(null == polylineRainbow) return
    colorValues = polylineRainbow.colors
}
