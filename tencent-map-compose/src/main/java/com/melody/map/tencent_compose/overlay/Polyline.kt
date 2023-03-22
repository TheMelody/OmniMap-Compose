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
import com.tencent.tencentmap.mapsdk.maps.model.AlphaAnimation
import com.tencent.tencentmap.mapsdk.maps.model.Animation
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.EmergeAnimation
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Polyline
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions.SegmentText

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
 * 线的分段颜色（彩虹线）配置
 */
class PolylineRainbow private constructor(
    val colors: List<Int>,
    val indexes: List<Int>
) {
    companion object {
        /**
         * 线的分段颜色（彩虹线）配置
         *
         * 【对应关系如下】:
         * colors: listOf(color1,color2,color3,color4)，indexes: listOf(0,1,2,3)
         *
         * 线上点【0, 1】之间颜色 => 取 color1
         *
         * 线上点【1, 2】之间颜色 => 取 color2
         *
         * 线上点【2, 3】之间颜色 => 取 color3
         *
         * 线上点 【3, 最后一个点】之间颜色 => 取 color4
         *
         * @param colors 每段索引之间的颜色，这个颜色同样支持纹理颜色
         * @param indexes 分段线的顶点索引，这个索引值的数量必须和colors颜色列表数量相同
         */
        fun create(colors: List<Int>, indexes: List<Int>): PolylineRainbow {
            if(colors.size != indexes.size) error("Error colors.size != indexes.size")
            return PolylineRainbow(colors, indexes)
        }
    }
}

/**
 * 沿线展示纹理图片
 *
 * 注意：内部使用的时候，已经给你默认调用了arrow(true)了，请放心使用。
 * @param arrowSpacing 线上的纹理的间距
 * @param arrowTexture 线上的纹理图(**叠加**纹理)
 */
class PolylineCustomTexture private constructor(
    val arrowSpacing: Int = 0,
    val arrowTexture: BitmapDescriptor? = null,
    val colorTexture: BitmapDescriptor? = null
) {
    companion object {
        /**
         * 调用这个方法，arrow必须打开这个开关，允许在线上绘制纹理；
         *
         * 注意：内部使用的时候，已经给你默认调用了arrow(true)了，请放心使用。
         * @param arrowSpacing 线上的纹理的间距
         * @param arrowTexture 线上的纹理图(**叠加**纹理)
         */
        fun create(arrowSpacing: Int, arrowTexture: BitmapDescriptor?): PolylineCustomTexture {
            return PolylineCustomTexture(
                arrowSpacing = arrowSpacing,
                arrowTexture = arrowTexture
            )
        }

        /**
         * 线路绘制的纹理图
         */
        fun create(colorTexture: BitmapDescriptor) : PolylineCustomTexture {
            return PolylineCustomTexture(colorTexture = colorTexture)
        }
    }
}

/**
 * 动态路名，线段上添加文字标注，文字可以作为线的属性在线上绘制出来
 */
class PolylineDynamicRoadName private constructor(
    val segmentTexts: List<SegmentText>,
) {
    /**
     * 每条线段上添加文字
     */
    fun create(segmentTexts: List<SegmentText>) : PolylineDynamicRoadName {
        return PolylineDynamicRoadName(segmentTexts = segmentTexts)
    }
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【普通线段】。
 *
 * 官方详细文档：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/drawLines
 *
 * @param points 线段的坐标点列表
 * @param appendPoints 在原有顶点上附加新的顶点
 * @param dynamicRoadName (可选)，线上动态路名，线段上添加文字标注，文字可以作为线的属性在线上绘制出来
 * @param polylineColor (可选，【不设置，则使用腾讯地图默认颜色】)线段的颜色
 * @param polylineBorderColor (可选，【不设置，则使用腾讯地图默认颜色】)线段边框的颜色，需要修改borderWidth才能生效
 * @param visible 线段的可见属性
 * @param lineType 线段的类型，必须是[PolylineOptions.LineType]里面的一种，如：[PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE]
 * @param pattern 设置虚线的样式，仅在：lineType = [PolylineOptions.LineType.LINE_TYPE_IMAGEINARYLINE]时才有效，pattern的元素数量必须是偶数个，每对元素分别表示虚线中实线区域的长度，以及空白区域的长度（单位px)，
 *        【注意】：设置虚线，还需要设置[polylineColor]和[polylineBorderColor]属性值，否则：无法覆盖默认的虚线颜色，或者无法显示虚线
 * @param useGradient 线段是否使用渐变色
 * @param isRoad 线段是否为路线
 * @param isLineCap 路线是否显示半圆端点
 * @param isClickable 是否可点击
 * @param animation 动画，目前仅支持[AlphaAnimation]或者[EmergeAnimation]
 * @param tag 线段的附件对象
 * @param width 线段宽度
 * @param borderWidth 线段边框的宽度，默认为0
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@TXMapComposable
fun Polyline(
    points: List<LatLng>,
    appendPoints: List<LatLng> = emptyList(),
    dynamicRoadName: PolylineDynamicRoadName? = null,
    polylineColor: Color? = null,
    polylineBorderColor: Color?  = null,
    visible: Boolean = true,
    useGradient: Boolean = false,
    isRoad: Boolean = true,
    isLineCap: Boolean = false,
    isClickable: Boolean = true,
    animation: Animation? = null,
    lineType: Int = LINE_TYPE_MULTICOLORLINE,
    pattern: List<Int> = listOf(),
    tag: Any? = null,
    width: Float = 10F,
    borderWidth: Float = 0F,
    zIndex: Float = 0F,
    onClick: (Polyline) -> Unit = {}
) {
    PolylineImpl(
        points = points,
        appendPoints = appendPoints,
        rainbow = null,
        customTexture = null,
        dynamicRoadName = dynamicRoadName,
        polylineColor = polylineColor,
        polylineBorderColor = polylineBorderColor,
        visible = visible,
        useGradient = useGradient,
        isRoad = isRoad,
        isLineCap = isLineCap,
        isClickable = isClickable,
        animation = animation,
        lineType = lineType,
        pattern = pattern,
        tag = tag,
        width = width,
        borderWidth = borderWidth,
        zIndex = zIndex,
        onClick = onClick
    )
}


/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【彩虹线段】
 *
 * 官方详细文档：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/drawLines
 *
 * @param points 线段的坐标点列表
 * @param appendPoints 在原有顶点上附加新的顶点
 * @param rainbow 线的分段颜色（彩虹线）
 * @param dynamicRoadName (可选)，线上动态路名，线段上添加文字标注，文字可以作为线的属性在线上绘制出来
 * @param visible 线段的可见属性
 * @param lineType 线段的类型，必须是[PolylineOptions.LineType]里面的一种，如：[PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE]
 * @param isRoad 线段是否为路线
 * @param useGradient 线段是否为渐变的彩虹线段，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param isLineCap 路线是否显示半圆端点
 * @param isClickable 是否可点击
 * @param animation 动画，目前仅支持[AlphaAnimation]或者[EmergeAnimation]
 * @param tag 线段的附件对象
 * @param width 线段宽度
 * @param borderWidth 线段边框的宽度，默认为0
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@TXMapComposable
fun PolylineRainbow(
    points: List<LatLng>,
    appendPoints: List<LatLng> = emptyList(),
    rainbow: PolylineRainbow?,
    useGradient: Boolean,
    dynamicRoadName: PolylineDynamicRoadName? = null,
    visible: Boolean = true,
    isRoad: Boolean = true,
    isLineCap: Boolean = false,
    isClickable: Boolean = true,
    animation: Animation? = null,
    lineType: Int = LINE_TYPE_MULTICOLORLINE,
    tag: Any? = null,
    width: Float = 10F,
    borderWidth: Float = 0F,
    zIndex: Float = 0F,
    onClick: (Polyline) -> Unit = {}
) {
    PolylineImpl(
        points = points,
        appendPoints = appendPoints,
        rainbow = rainbow,
        customTexture = null,
        dynamicRoadName = dynamicRoadName,
        polylineColor = null,
        polylineBorderColor = null,
        visible = visible,
        useGradient = useGradient,
        isRoad = isRoad,
        isLineCap = isLineCap,
        isClickable = isClickable,
        animation = animation,
        lineType = lineType,
        pattern = null,
        tag = tag,
        width = width,
        borderWidth = borderWidth,
        zIndex = zIndex,
        onClick = onClick
    )
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【纹理线段】
 *
 * 官方详细文档：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/drawLines
 *
 * @param points 线段的坐标点列表
 * @param appendPoints 在原有顶点上附加新的顶点
 * @param customTexture (**注意：arrowTexture不支持二次更新**)，线上自定义的纹理，如：**叠加**纹理图
 * @param dynamicRoadName (可选)，线上动态路名，线段上添加文字标注，文字可以作为线的属性在线上绘制出来
 * @param polylineColor (可选，【不设置，则使用腾讯地图默认颜色】)线段的颜色
 * @param polylineBorderColor (可选，【不设置，则使用腾讯地图默认颜色】)线段边框的颜色，需要修改borderWidth才能生效
 * @param visible 线段的可见属性
 * @param lineType 线段的类型，必须是[PolylineOptions.LineType]里面的一种，如：[PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE]
 * @param isRoad 线段是否为路线
 * @param isLineCap 路线是否显示半圆端点
 * @param isClickable 是否可点击
 * @param animation 动画，目前仅支持[AlphaAnimation]或者[EmergeAnimation]
 * @param tag 线段的附件对象
 * @param width 线段宽度
 * @param borderWidth 线段边框的宽度，默认为0
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@TXMapComposable
fun PolylineCustomTexture(
    points: List<LatLng>,
    appendPoints: List<LatLng> = emptyList(),
    customTexture: PolylineCustomTexture?,
    dynamicRoadName: PolylineDynamicRoadName? = null,
    polylineColor: Color? = null,
    polylineBorderColor: Color?  = null,
    visible: Boolean = true,
    isRoad: Boolean = true,
    isLineCap: Boolean = false,
    isClickable: Boolean = true,
    animation: Animation? = null,
    lineType: Int = LINE_TYPE_MULTICOLORLINE,
    tag: Any? = null,
    width: Float = 10F,
    borderWidth: Float = 0F,
    zIndex: Float = 0F,
    onClick: (Polyline) -> Unit = {}
) {
    PolylineImpl(
        points = points,
        appendPoints = appendPoints,
        rainbow = null,
        customTexture = customTexture,
        dynamicRoadName = dynamicRoadName,
        polylineColor = polylineColor,
        polylineBorderColor = polylineBorderColor,
        visible = visible,
        useGradient = true,
        isRoad = isRoad,
        isLineCap = isLineCap,
        isClickable = isClickable,
        animation = animation,
        lineType = lineType,
        pattern = null,
        tag = tag,
        width = width,
        borderWidth = borderWidth,
        zIndex = zIndex,
        onClick = onClick
    )
}

/**
 * 【Polyline实现类】地图线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * 官方详细文档：https://lbs.qq.com/mobile/androidMapSDK/developerGuide/drawLines
 *
 * @param points 线段的坐标点列表
 * @param appendPoints 在原有顶点上附加新的顶点
 * @param rainbow (可选)，线的分段颜色（彩虹线）
 * @param customTexture (**注意：arrowTexture不支持二次更新**)，线上自定义的纹理，如：叠加纹理图
 * @param dynamicRoadName (可选)，线上动态路名，线段上添加文字标注，文字可以作为线的属性在线上绘制出来
 * @param polylineColor (可选，【不设置，则使用腾讯地图默认颜色】)线段的颜色
 * @param polylineBorderColor (可选，【不设置，则使用腾讯地图默认颜色】)线段边框的颜色，需要修改borderWidth才能生效
 * @param visible 线段的可见属性
 * @param lineType 线段的类型，必须是[PolylineOptions.LineType]里面的一种，如：[PolylineOptions.LineType.LINE_TYPE_MULTICOLORLINE]
 * @param pattern 设置虚线的样式，仅在：lineType = [PolylineOptions.LineType.LINE_TYPE_IMAGEINARYLINE]时才有效，pattern的元素数量必须是偶数个，每对元素分别表示虚线中实线区域的长度，以及空白区域的长度（单位px)，
 *        【注意】：设置虚线，还需要设置[polylineColor]和[polylineBorderColor]属性值，否则：无法覆盖默认的虚线颜色，或者无法显示虚线
 * @param useGradient 线段是否为渐变的彩虹线段【默认为true】，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param isRoad 线段是否为路线
 * @param isLineCap 路线是否显示半圆端点
 * @param isClickable 是否可点击
 * @param animation 动画，目前仅支持[AlphaAnimation]或者[EmergeAnimation]
 * @param tag 线段的附件对象
 * @param width 线段宽度
 * @param borderWidth 线段边框的宽度，默认为0
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@TXMapComposable
private fun PolylineImpl(
    points: List<LatLng>,
    appendPoints: List<LatLng>,
    rainbow: PolylineRainbow?,
    customTexture: PolylineCustomTexture?,
    dynamicRoadName: PolylineDynamicRoadName?,
    polylineColor: Color?,
    polylineBorderColor: Color?,
    visible: Boolean,
    useGradient: Boolean,
    isRoad: Boolean,
    isLineCap: Boolean,
    isClickable: Boolean,
    animation: Animation?,
    lineType: Int,
    pattern: List<Int>?,
    tag: Any?,
    width: Float,
    borderWidth: Float,
    zIndex: Float,
    onClick: (Polyline) -> Unit
) {
    if(null != animation && !(animation is AlphaAnimation || animation is EmergeAnimation)) {
        error("animation must be either AlphaAnimation or EmergeAnimation")
    }
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addPolyline (
                PolylineOptions().apply {
                    addAll(points)
                    lineCap(isLineCap)
                    polylineColor?.let { color(polylineColor.toArgb()) }
                    if(borderWidth > 0 && null != polylineBorderColor){
                        borderColors(intArrayOf(polylineBorderColor.toArgb()))
                    }
                    gradient(useGradient)
                    if(useGradient) {
                        // 这里规避下，如果外部设置为true，则必须设置下面这个类型，且road也必须为true
                        lineType(LINE_TYPE_MULTICOLORLINE)
                        road(true)
                    } else {
                        lineType(lineType)
                        road(isRoad)
                    }
                    if(pattern?.isNotEmpty() == true) {
                        // 线段虚线样式
                        pattern(pattern)
                    }
                    clickable(isClickable)
                    visible(visible)
                    width(width)
                    borderWidth(borderWidth)
                    customTexture(customTexture)
                }) ?: error("Error adding Polyline")
            polyline.tag = tag
            polyline.rainbowColorLine(rainbow)
            polyline.dynamicRoadName(dynamicRoadName)
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            set(points) { this.polyline.points = it }
            set(appendPoints) {
                if(it.isNotEmpty()) {
                    this.polyline.appendPoints(it)
                }
            }
            set(polylineColor) { it?.let { this.polyline.color = it.toArgb() } }
            set(polylineBorderColor) {
                if(borderWidth > 0 && null != polylineBorderColor){
                    this.polyline.setBorderColors(intArrayOf(polylineBorderColor.toArgb()))
                }
            }
            set(tag) { this.polyline.tag = it }
            set(rainbow) { this.polyline.rainbowColorLine(it) }
            set(useGradient) { this.polyline.isGradientEnable = it }
            set(dynamicRoadName) { this.polyline.dynamicRoadName(it) }
            set(customTexture) { this.polyline.customTexture(it) }
            set(visible) { this.polyline.isVisible = it }
            set(isClickable) { this.polyline.isClickable = it }
            set(animation) { this.polyline.startAnimation(it) }
            set(width) { this.polyline.width = it }
            set(zIndex) { this.polyline.setZIndex(it) }
        }
    )
}

/**
 * 设置彩虹线
 */
private fun Polyline.rainbowColorLine(polylineRainbow: PolylineRainbow?) {
    if(null == polylineRainbow) return
    setColors(polylineRainbow.colors.toIntArray(), polylineRainbow.indexes.toIntArray())
}

/**
 * PolylineOptions自定义线上纹理图
 */
private fun PolylineOptions.customTexture(customInfo: PolylineCustomTexture?) {
    if(null == customInfo) return
    if(null != customInfo.colorTexture) {
        // 线路纹理图
        colorTexture(customInfo.colorTexture)
        return
    }
    if(null != customInfo.arrowTexture) {
        // 叠加的纹理图
        arrow(true).arrowSpacing(customInfo.arrowSpacing).arrowTexture(customInfo.arrowTexture)
    }
}
/**
 * Polyline自定义线上纹理图
 */
private fun Polyline.customTexture(customInfo: PolylineCustomTexture?) {
    if(null == customInfo) return
    if(null != customInfo.colorTexture) {
        // 线路纹理图
        setColorTexture(customInfo.colorTexture)
        return
    }
    if(null != customInfo.arrowTexture) {
        // 叠加的纹理图
        setArrow(true)
        arrowSpacing(customInfo.arrowSpacing)
        // arrowTexture没有提供，不支持二次更新
        //arrowTexture(customInfo.arrowTexture)
    }
}
/**
 * 动态路名
 */
private fun Polyline.dynamicRoadName(dynamicRoadName: PolylineDynamicRoadName?) {
    if(null == dynamicRoadName) return
    // 每条线段上添加文字
    text = PolylineOptions.Text.Builder(dynamicRoadName.segmentTexts).build()
}