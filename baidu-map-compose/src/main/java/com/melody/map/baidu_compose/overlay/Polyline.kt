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
import com.baidu.mapapi.map.Polyline
import com.baidu.mapapi.map.PolylineDottedLineType
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.map.PolylineOptions.LineCapType
import com.baidu.mapapi.map.PolylineOptions.LineJoinType
import com.baidu.mapapi.map.PolylineOptions.LineDirectionCross180
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class PolylineNode(
    val polyline: Polyline,
    var onPolylineClick: (Polyline) -> Boolean
) : MapNode {
    override fun onRemoved() {
        polyline.remove()
    }
}

/**
 * 线的分段颜色（彩虹线）配置
 *
 * @param colors 每段索引之间的颜色
 * @param points 线段的顶点坐标
 */
class PolylineRainbow private constructor(
    val colors: List<Int>,
    val points: List<LatLng>
) {
    companion object {
        /**
         * 线的分段颜色（彩虹线）配置， 需要与setPoints(List)一起使用，并且在setPoints(List)之前执行，否则 该方法更改颜色不生效。
         *
         * 折线每个点的颜色值，每一个点带一个颜色值
         *
         * @param colors 每段索引之间的颜色
         * @param points 线段的顶点坐标
         */
        fun create(colors: List<Int>, points: List<LatLng>): PolylineRainbow {
            if(colors.size < points.size) error("Error colors.size < points.size")
            return PolylineRainbow(colors, points)
        }
    }
}

/**
 * 沿线展示纹理图片
 * @param isKeepScale 设置纹理宽、高是否保持原比例渲染，默认false
 * @param points 设置线段的顶点坐标
 * @param textureList (可选，单个重复的纹理图请使用**texture**参数)纹理图片列表
 * @param indexList (可选，保证和textureList大小一致)纹理图片索引
 * @param texture (可选，不用texture就用textureList)纹理图片
 */
class PolylineCustomTexture private constructor(
    val isKeepScale: Boolean,
    val points: List<LatLng>,
    val textureList: List<BitmapDescriptor>?,
    val texture: BitmapDescriptor?,
    val indexList : List<Int>?
) {
    companion object {
        /**
         * 分段纹理绘制折线， 需要与setPoints(List)一起使用，并且在setPoints(List)之前执行，否则 该方法更改indexList/textureList不生效。
         * @param isKeepScale 设置纹理宽、高是否保持原比例渲染，默认false
         * @param points 设置线段的顶点坐标
         * @param textureList 纹理图片列表
         * @param indexList 纹理图片索引
         */
        fun create(
            isKeepScale: Boolean = false,
            points: List<LatLng>,
            textureList: List<BitmapDescriptor>,
            indexList: List<Int>
        ): PolylineCustomTexture {
            if(textureList.size != indexList.size) error("Error textureList.size != indexList.size")
            return PolylineCustomTexture(
                isKeepScale = isKeepScale,
                points = points,
                textureList = textureList,
                texture = null,
                indexList = indexList,
            )
        }

        /**
         * 纹理绘制折线， 需要与setPoints(List)一起使用
         * @param isKeepScale 设置纹理宽、高是否保持原比例渲染，默认false
         * @param points 设置线段的顶点坐标
         * @param texture 纹理图片
         */
        fun create(
            isKeepScale: Boolean = false,
            points: List<LatLng>,
            texture: BitmapDescriptor?
        ): PolylineCustomTexture {
            return PolylineCustomTexture(
                isKeepScale = isKeepScale,
                points = points,
                texture = texture,
                textureList = null,
                indexList = null,
            )
        }
    }
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【普通线段】。
 *
 * @param points 线段的坐标点列表
 * @param lineJoinType 设置Polyline的拐点连接类型[LineJoinType]
 * @param lineCapType  设置Polyline的端点类型[LineCapType]
 * @param polylineColor (可选，【不设置，则使用百度地图默认颜色】)线段的颜色
 * @param lineDirectionCross180 设置Polyline跨越180度的方向。默认: [LineDirectionCross180.NONE], 不跨越180度
 * @param dottedLineType 设置Polyline的虚线类型[PolylineDottedLineType]
 * @param geodesic 是否绘制为大地曲线,默认为false
 * @param visible 线段的可见属性
 * @param isThined 是否需要对Polyline坐标数据进行抽稀, 默认抽稀
 * @param isDottedLine 设置Polyline是否为虚线
 * @param isClickable 是否可点击
 * @param width 线段宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调，true拦截事件，不继续往下传递，false事件可以继续往下传递到地图
 */
@Composable
@BDMapComposable
fun Polyline(
    points: List<LatLng>,
    polylineColor: Color? = null,
    visible: Boolean = true,
    geodesic: Boolean = false,
    isThined: Boolean = true,
    isDottedLine: Boolean = false,
    dottedLineType: PolylineDottedLineType? = null,
    lineJoinType: LineJoinType = LineJoinType.LineJoinBevel,
    lineCapType: LineCapType = LineCapType.LineCapRound,
    lineDirectionCross180: LineDirectionCross180 = LineDirectionCross180.NONE,
    isClickable: Boolean = true,
    width: Int = 10,
    zIndex: Int = 0,
    onClick: (Polyline) -> Boolean = { false }
) {
    PolylineImpl(
        points = points,
        rainbow = null,
        customTexture = null,
        polylineColor = polylineColor,
        visible = visible,
        geodesic = geodesic,
        isThined = isThined,
        isDottedLine = isDottedLine,
        useGradient = false,
        dottedLineType = dottedLineType,
        isClickable = isClickable,
        lineJoinType = lineJoinType,
        lineCapType = lineCapType,
        lineDirectionCross180 = lineDirectionCross180,
        width = width,
        zIndex = zIndex,
        onClick = onClick
    )
}


/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【彩虹线段】
 *
 * @param rainbow (可选)，线的分段颜色（彩虹线）
 * @param lineJoinType 设置Polyline的拐点连接类型[LineJoinType]
 * @param lineCapType  设置Polyline的端点类型[LineCapType]
 * @param lineDirectionCross180 设置Polyline跨越180度的方向。默认: [LineDirectionCross180.NONE], 不跨越180度
 * @param dottedLineType 设置Polyline的虚线类型[PolylineDottedLineType]
 * @param geodesic 是否绘制为大地曲线,默认为false
 * @param visible 线段的可见属性
 * @param isThined 是否需要对Polyline坐标数据进行抽稀, 默认抽稀
 * @param isDottedLine 设置Polyline是否为虚线
 * @param useGradient 线段是否为渐变的彩虹线段【默认为true】，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param isClickable 是否可点击
 * @param width 线段宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调，true拦截事件，不继续往下传递，false事件可以继续往下传递到地图
 */
@Composable
@BDMapComposable
fun PolylineRainbow(
    rainbow: PolylineRainbow?,
    useGradient: Boolean = false,
    isDottedLine: Boolean = false,
    dottedLineType: PolylineDottedLineType? = null,
    lineJoinType: LineJoinType = LineJoinType.LineJoinBevel,
    lineCapType: LineCapType = LineCapType.LineCapRound,
    lineDirectionCross180: LineDirectionCross180 = LineDirectionCross180.NONE,
    geodesic: Boolean = false,
    visible: Boolean = true,
    isThined: Boolean = true,
    isClickable: Boolean = true,
    width: Int = 10,
    zIndex: Int = 0,
    onClick: (Polyline) -> Boolean = { false }
) {
    PolylineImpl(
        rainbow = rainbow,
        points = null,
        polylineColor = null,
        customTexture = null,
        useGradient = useGradient,
        isClickable = isClickable,
        isDottedLine = isDottedLine,
        dottedLineType = dottedLineType,
        lineCapType = lineCapType,
        lineJoinType = lineJoinType,
        lineDirectionCross180 = lineDirectionCross180,
        geodesic = geodesic,
        visible = visible,
        isThined = isThined,
        width = width,
        zIndex = zIndex,
        onClick = onClick
    )
}

/**
 * 地图线段覆盖物。一个线段是多个连贯点的集合【纹理线段】
 *
 * @param customTexture (可选)，线上自定义的纹理，如：叠加纹理图
 * @param isDottedLine 设置Polyline是否为虚线，**分段纹理绘制折线时建议开启绘制虚线，不是分段纹理，则不需要开启**
 * @param dottedLineType 设置Polyline的虚线类型[PolylineDottedLineType]
 * @param lineJoinType 设置Polyline的拐点连接类型[LineJoinType]
 * @param lineCapType  设置Polyline的端点类型[LineCapType]
 * @param lineDirectionCross180 设置Polyline跨越180度的方向。默认: [LineDirectionCross180.NONE], 不跨越180度
 * @param geodesic 是否绘制为大地曲线【默认为false】
 * @param visible 线段的可见属性【默认为true】
 * @param isThined 是否需要对Polyline坐标数据进行抽稀, 默认抽稀
 * @param useGradient 线段是否为渐变的线段【默认为false】
 * @param isClickable 是否可点击
 * @param width 线段宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调，true拦截事件，不继续往下传递，false事件可以继续往下传递到地图
 */
@Composable
@BDMapComposable
fun PolylineCustomTexture(
    customTexture: PolylineCustomTexture?,
    isDottedLine: Boolean = false,
    dottedLineType: PolylineDottedLineType? = null,
    lineJoinType: LineJoinType = LineJoinType.LineJoinBevel,
    lineCapType: LineCapType = LineCapType.LineCapRound,
    lineDirectionCross180: LineDirectionCross180 = LineDirectionCross180.NONE,
    geodesic: Boolean = false,
    visible: Boolean = true,
    isThined: Boolean = true,
    useGradient: Boolean = false,
    isClickable: Boolean = true,
    width: Int = 10,
    zIndex: Int = 0,
    onClick: (Polyline) -> Boolean = { false }
) {
    PolylineImpl(
        points = null,
        rainbow = null,
        polylineColor = null,
        customTexture = customTexture,
        useGradient = useGradient,
        isClickable = isClickable,
        isDottedLine = isDottedLine,
        dottedLineType = dottedLineType,
        lineCapType = lineCapType,
        lineJoinType = lineJoinType,
        lineDirectionCross180 = lineDirectionCross180,
        geodesic = geodesic,
        visible = visible,
        isThined = isThined,
        width = width,
        zIndex = zIndex,
        onClick = onClick
    )
}

/**
 * 【Polyline实现类】地图线段覆盖物。一个线段是多个连贯点的集合线段。
 *
 * @param points 线段的坐标点列表
 * @param rainbow (可选)，线的分段颜色（彩虹线）
 * @param customTexture (可选)，线上自定义的纹理，如：叠加纹理图
 * @param lineJoinType 设置Polyline的拐点连接类型[LineJoinType]
 * @param lineCapType  设置Polyline的端点类型[LineCapType]
 * @param polylineColor (可选，【不设置，则使用百度地图默认颜色】)线段的颜色
 * @param lineDirectionCross180 设置Polyline跨越180度的方向。默认: [LineDirectionCross180.NONE], 不跨越180度
 * @param dottedLineType 设置Polyline的虚线类型[PolylineDottedLineType]
 * @param geodesic 是否绘制为大地曲线,默认为false
 * @param visible 线段的可见属性
 * @param isThined 是否需要对Polyline坐标数据进行抽稀, 默认抽稀
 * @param isDottedLine 设置Polyline是否为虚线
 * @param useGradient 线段是否为渐变的彩虹线段【默认为true】，如果设置为false，颜色一块是一块，如果设置为true，线段是多个颜色渐变连贯的
 * @param isClickable 是否可点击
 * @param width 线段宽度
 * @param zIndex 显示层级
 * @param onClick polyline点击事件回调
 */
@Composable
@BDMapComposable
private fun PolylineImpl(
    points: List<LatLng>?,
    rainbow: PolylineRainbow?,
    customTexture: PolylineCustomTexture?,
    lineJoinType: LineJoinType,
    lineCapType: LineCapType,
    polylineColor: Color?,
    lineDirectionCross180: LineDirectionCross180,
    dottedLineType: PolylineDottedLineType?,
    geodesic: Boolean,
    visible: Boolean,
    isThined: Boolean,
    isDottedLine: Boolean,
    useGradient: Boolean,
    isClickable: Boolean,
    width: Int,
    zIndex: Int,
    onClick: (Polyline) -> Boolean
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addOverlay(
                PolylineOptions().apply {
                    customTexture(customTexture)
                    polylineColor?.let { color(polylineColor.toArgb()) }
                    rainbowColorLine(rainbow)
                    lineDirectionCross180(lineDirectionCross180)
                    points?.let { points(it) }
                    isGeodesic(geodesic)
                    isThined(isThined)
                    dottedLine(isDottedLine)
                    dottedLineType?.let { dottedLineType(it) }
                    lineCapType(lineCapType)
                    lineJoinType(lineJoinType)
                    isGradient(useGradient)
                    clickable(isClickable)
                    visible(visible)
                    width(width)
                }) as? Polyline ?: error("Error adding Polyline")
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            set(points) { it?.let { this.polyline.points = it }  }
            set(rainbow) { this.polyline.rainbowColorLine(it) }
            set(lineCapType) { this.polyline.lineCapType = it }
            set(lineJoinType) { this.polyline.lineJoinType = it }
            set(isDottedLine) { this.polyline.isDottedLine = it }
            set(dottedLineType) { it?.let { this.polyline.setDottedLineType(it) }  }
            set(polylineColor) { it?.let { this.polyline.color = it.toArgb() } }
            set(useGradient) { this.polyline.isGradient = it }
            set(lineDirectionCross180) { this.polyline.lineDirectionCross180 = it }
            set(geodesic) { this.polyline.isGeodesic = it }
            set(customTexture) { this.polyline.customTexture(it) }
            set(isThined) { this.polyline.isThined = it }
            set(visible) { this.polyline.isVisible = it }
            set(isClickable) { this.polyline.isClickable = it }
            set(width) { this.polyline.width = it }
            set(zIndex) { this.polyline.zIndex = it }
        }
    )
}

/**
 * Polyline设置彩虹线
 */
private fun Polyline.rainbowColorLine(polylineRainbow: PolylineRainbow?) {
    if(null == polylineRainbow) return
    colorList = polylineRainbow.colors.toIntArray()
    points = polylineRainbow.points
}
/**
 * PolylineOptions设置彩虹线
 */
private fun PolylineOptions.rainbowColorLine(polylineRainbow: PolylineRainbow?) {
    if(null == polylineRainbow) return
    colorsValues(polylineRainbow.colors)
    points(polylineRainbow.points)
}

/**
 * PolylineOptions自定义线上纹理图
 */
private fun PolylineOptions.customTexture(customInfo: PolylineCustomTexture?) {
    if(null == customInfo) return
    keepScale(customInfo.isKeepScale)
    if(customInfo.texture != null) {
        customTexture(customInfo.texture)
        points(customInfo.points)
        return
    }
    if(customInfo.indexList?.isNotEmpty() == true) {
        customTextureList(customInfo.textureList)
        textureIndex(customInfo.indexList)
        points(customInfo.points)
    }
}
/**
 * Polyline自定义线上纹理图
 */
private fun Polyline.customTexture(customInfo: PolylineCustomTexture?) {
    if(null == customInfo) return
    isIsKeepScale = customInfo.isKeepScale
    if(customInfo.texture != null) {
        texture = customInfo.texture
        points = customInfo.points
        return
    }
    if(customInfo.indexList?.isNotEmpty() == true) {
        setTextureList(customInfo.textureList)
        setIndexs(customInfo.indexList.toIntArray())
        points = customInfo.points
    }
}