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

package com.melody.map.baidu_compose.poperties

import android.graphics.Rect
import java.util.*

internal val DefaultMapUiSettings = MapUiSettings()

/**
 * 与UI相关设置的数据类
 * @param mapViewPadding 地图上控件与地图边界的距离，包含比例尺、缩放控件、logo、指南针的位置 只有在 OnMapLoadedCallback.onMapLoaded() 之后设置才生效
 * @param isRotateGesturesEnabled 旋转手势是否可用
 * @param isScrollGesturesEnabled 拖拽手势是否可用
 * @param isTiltGesturesEnabled 倾斜手势(等价于：地图【俯视手势】（3D）)是否可用
 * @param isZoomGesturesEnabled 缩放手势是否可用
 * @param isDoubleClickZoomEnabled 是否允许双击放大地图手势
 * @param isFlingEnable 是否允许抛出手势，默认为true，只要启用了[isScrollGesturesEnabled]就可以直接使用了
 * @param isInertialAnimation 是否打开缩放动画惯性，默认为true
 * @param isCompassEnabled 指南针控件是否可见
 * @param isZoomEnabled 是否显示缩放按钮
 * @param isScaleControlsEnabled 是否显示比例尺
 */
class MapUiSettings(
    val mapViewPadding: Rect = Rect(),
    val isRotateGesturesEnabled: Boolean = false,
    val isScrollGesturesEnabled: Boolean = false,
    val isTiltGesturesEnabled: Boolean = false,
    val isZoomGesturesEnabled: Boolean = false,
    val isDoubleClickZoomEnabled: Boolean = false,
    val isFlingEnable: Boolean = true,
    val isInertialAnimation: Boolean = true,
    val isCompassEnabled: Boolean = false,
    val isZoomEnabled: Boolean = false,
    val isScaleControlsEnabled: Boolean = false
) {

    override fun equals(other: Any?): Boolean = other is MapUiSettings &&
            mapViewPadding == other.mapViewPadding &&
            isRotateGesturesEnabled == other.isRotateGesturesEnabled &&
            isScrollGesturesEnabled == other.isScrollGesturesEnabled &&
            isTiltGesturesEnabled == other.isTiltGesturesEnabled &&
            isZoomGesturesEnabled == other.isZoomGesturesEnabled &&
            isDoubleClickZoomEnabled == other.isDoubleClickZoomEnabled &&
            isFlingEnable == other.isFlingEnable &&
            isInertialAnimation == other.isInertialAnimation &&
            isCompassEnabled == other.isCompassEnabled &&
            isZoomEnabled == other.isZoomEnabled &&
            isScaleControlsEnabled == other.isScaleControlsEnabled

    override fun hashCode(): Int = Objects.hash(
        mapViewPadding,
        isRotateGesturesEnabled,
        isScrollGesturesEnabled,
        isTiltGesturesEnabled,
        isZoomGesturesEnabled,
        isDoubleClickZoomEnabled,
        isFlingEnable,
        isInertialAnimation,
        isCompassEnabled,
        isZoomEnabled,
        isScaleControlsEnabled
    )

    fun copy(
        mapViewPadding: Rect = this.mapViewPadding,
        isRotateGesturesEnabled: Boolean = this.isRotateGesturesEnabled,
        isScrollGesturesEnabled: Boolean = this.isScrollGesturesEnabled,
        isTiltGesturesEnabled: Boolean = this.isTiltGesturesEnabled,
        isZoomGesturesEnabled: Boolean = this.isZoomGesturesEnabled,
        isDoubleClickZoomEnabled: Boolean = this.isDoubleClickZoomEnabled,
        isFlingEnable: Boolean = this.isFlingEnable,
        isInertialAnimation: Boolean = this.isInertialAnimation,
        isCompassEnabled: Boolean = this.isCompassEnabled,
        isZoomEnabled: Boolean = this.isZoomEnabled,
        isScaleControlsEnabled: Boolean = this.isScaleControlsEnabled
    ): MapUiSettings = MapUiSettings(
        mapViewPadding = mapViewPadding,
        isRotateGesturesEnabled = isRotateGesturesEnabled,
        isScrollGesturesEnabled = isScrollGesturesEnabled,
        isTiltGesturesEnabled = isTiltGesturesEnabled,
        isZoomGesturesEnabled = isZoomGesturesEnabled,
        isDoubleClickZoomEnabled = isDoubleClickZoomEnabled,
        isFlingEnable = isFlingEnable,
        isInertialAnimation = isInertialAnimation,
        isCompassEnabled = isCompassEnabled,
        isZoomEnabled = isZoomEnabled,
        isScaleControlsEnabled = isScaleControlsEnabled
    )

    override fun toString(): String {
        return "MapUiSettings(" +
                "mapViewPadding=$mapViewPadding, " +
                "isRotateGesturesEnabled=$isRotateGesturesEnabled, " +
                "isScrollGesturesEnabled=$isScrollGesturesEnabled," +
                "isTiltGesturesEnabled=$isTiltGesturesEnabled, " +
                "isZoomGesturesEnabled=$isZoomGesturesEnabled, " +
                "isDoubleClickZoomEnabled=$isDoubleClickZoomEnabled, " +
                "isFlingEnable=$isFlingEnable, " +
                "isInertialAnimation=$isInertialAnimation, " +
                "isZoomEnabled=$isZoomEnabled, " +
                "isScaleControlsEnabled=$isScaleControlsEnabled, " +
                "isCompassEnabled=$isCompassEnabled)"
    }


}

