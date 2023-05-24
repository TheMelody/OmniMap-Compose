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
import com.baidu.mapapi.map.Circle
import com.baidu.mapapi.map.CircleDottedStrokeType
import com.baidu.mapapi.map.CircleDottedStrokeType.DOTTED_LINE_CIRCLE
import com.baidu.mapapi.map.CircleHoleOptions
import com.baidu.mapapi.map.CircleOptions
import com.baidu.mapapi.map.Stroke
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.MapNode
import com.melody.map.baidu_compose.model.BDMapComposable

internal class CircleNode(
    val circle: Circle
) : MapNode {
    override fun onRemoved() {
        circle.remove()
    }
}

/**
 * 在地图上绘制圆的覆盖物.
 *
 * @param center 圆心经纬度坐标
 * @param fillColor 圆的填充颜色
 * @param radius 圆的半径，单位米.
 * @param isDottedStroke 是否绘制虚线圆边框
 * @param dottedStrokeType  圆的虚线Stroke类型
 * @param holeOptions   镂空效果
 * @param strokeColor 圆的边框颜色
 * @param strokeWidth 圆的边框宽度
 * @param visible 圆是否可见
 * @param zIndex 设置显示的层级，越大越靠上显示
 */
@Composable
@BDMapComposable
fun Circle(
    center: LatLng,
    fillColor: Color,
    strokeColor: Color,
    radius: Int,
    isDottedStroke: Boolean = false,
    dottedStrokeType: CircleDottedStrokeType = DOTTED_LINE_CIRCLE,
    holeOptions: CircleHoleOptions? = null,
    strokeWidth: Int = 10,
    visible: Boolean = true,
    zIndex: Int = 0,
) {
    CircleImpl(
        center = center,
        fillColor = fillColor,
        radius = radius,
        useGradientCircle = false,
        centerColor = Color.Transparent,
        sideColor = Color.Transparent,
        isDottedStroke = isDottedStroke,
        dottedStrokeType = dottedStrokeType,
        holeOptions = holeOptions,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        visible = visible,
        zIndex = zIndex
    )
}

/**
 * 在地图上绘制渐变色圆的覆盖物.
 *
 * @param center 圆心经纬度坐标
 * @param radius 圆的半径，单位米.
 * @param centerColor 渐变色圆中心的颜色
 * @param sideColor  centerColor渐变至sideColor
 * @param isDottedStroke 是否绘制虚线圆边框
 * @param dottedStrokeType  圆的虚线Stroke类型
 * @param strokeColor 圆的边框颜色
 * @param strokeWidth 圆的边框宽度
 * @param visible 圆是否可见
 * @param zIndex 设置显示的层级，越大越靠上显示
 */
@Composable
@BDMapComposable
fun CircleGradient(
    center: LatLng,
    radius: Int,
    centerColor: Color,
    sideColor: Color,
    isDottedStroke: Boolean = false,
    dottedStrokeType: CircleDottedStrokeType = DOTTED_LINE_CIRCLE,
    strokeColor: Color,
    strokeWidth: Int = 10,
    visible: Boolean = true,
    zIndex: Int = 0,
) {
    CircleImpl(
        center = center,
        fillColor = Color.Transparent,
        radius = radius,
        useGradientCircle = true,
        centerColor = centerColor,
        sideColor = sideColor,
        isDottedStroke = isDottedStroke,
        dottedStrokeType = dottedStrokeType,
        holeOptions = null,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        visible = visible,
        zIndex = zIndex
    )
}

/*
 * CircleImpl
 * @param center 圆心经纬度坐标
 * @param radius 圆的半径，单位米.
 * @param useGradientCircle 是否设置渐变色的圆
 * @param centerColor 渐变色圆中心的颜色
 * @param sideColor  centerColor渐变至sideColor
 * @param holeOptions   镂空效果
 * @param isDottedStroke 是否绘制虚线圆边框
 * @param dottedStrokeType  圆的虚线Stroke类型
 * @param strokeColor 圆的边框颜色
 * @param strokeWidth 圆的边框宽度
 * @param visible 圆是否可见
 * @param zIndex 设置显示的层级，越大越靠上显示
 */
@Composable
@BDMapComposable
private fun CircleImpl(
    center: LatLng,
    fillColor: Color,
    radius: Int,
    useGradientCircle: Boolean,
    centerColor: Color,
    sideColor: Color,
    holeOptions: CircleHoleOptions?,
    isDottedStroke: Boolean,
    dottedStrokeType: CircleDottedStrokeType,
    strokeColor: Color,
    strokeWidth: Int,
    visible: Boolean,
    zIndex: Int
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val circle = mapApplier?.map?.addOverlay(
                CircleOptions().apply  {
                    center(center)
                    radius(radius)
                    dottedStroke(isDottedStroke)
                    dottedStrokeType(dottedStrokeType)
                    stroke(Stroke(strokeWidth,strokeColor.toArgb()))
                    isIsGradientCircle = useGradientCircle
                    if(!useGradientCircle) {
                        // 如果设置了渐变，则不支持设置填充色和区域内镂空
                        fillColor(fillColor.toArgb())
                        holeOptions?.let { addHoleOption(it) }
                    } else {
                        setCenterColor(centerColor.toArgb())
                        setSideColor(sideColor.toArgb())
                    }
                    visible(visible)
                    zIndex(zIndex)
                }
            ) as? Circle ?: error("Error adding circle")
            CircleNode(circle)
        },
        update = {
            set(center) { this.circle.center = it }
            set(fillColor) {
                if(!useGradientCircle){
                    this.circle.fillColor = it.toArgb()
                }
            }
            set(radius) { this.circle.radius = it }
            set(strokeColor) { this.circle.stroke = Stroke(strokeWidth,it.toArgb()) }
            set(strokeWidth) { this.circle.stroke = Stroke(it, strokeColor.toArgb()) }
            set(dottedStrokeType) { this.circle.setDottedStrokeType(it) }
            set(isDottedStroke) { this.circle.isDottedStroke = it }
            set(useGradientCircle) { this.circle.isIsGradientCircle = it }
            set(holeOptions) {
                if(!useGradientCircle && null != it) {
                    this.circle.holeOption = it
                }
            }
            set(centerColor) {
                if(useGradientCircle){
                    this.circle.centerColor = it.toArgb()
                }
            }
            set(sideColor) {
                if(useGradientCircle){
                    this.circle.sideColor = it.toArgb()
                }
            }
            set(visible) { this.circle.isVisible = it }
            set(zIndex) { this.circle.zIndex = it }
        }
    )
}