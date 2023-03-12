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

package com.melody.map.gd_compose.poperties

import java.util.*

internal val DefaultMapUiSettings = MapUiSettings()

/**
 * 与UI相关设置的数据类
 * @param isRotateGesturesEnabled 旋转手势是否可用
 * @param isScrollGesturesEnabled 拖拽手势是否可用
 * @param isTiltGesturesEnabled 倾斜手势是否可用
 * @param isZoomGesturesEnabled 缩放手势是否可用
 * @param isZoomEnabled 缩放按钮是否可见
 * @param isCompassEnabled 指南针控件是否可见
 * @param myLocationButtonEnabled 设置默认定位按钮是否显示，非必需设置。
 * @param isScaleControlsEnabled 比例尺控件是否可见
 */
class MapUiSettings(
    val isRotateGesturesEnabled: Boolean = false,
    val isScrollGesturesEnabled: Boolean = false,
    val isTiltGesturesEnabled: Boolean = false,
    val isZoomGesturesEnabled: Boolean = false,
    val isZoomEnabled: Boolean = false,
    val isCompassEnabled: Boolean = false,
    val myLocationButtonEnabled: Boolean = false,
    val isScaleControlsEnabled: Boolean = false
) {

    override fun equals(other: Any?): Boolean = other is MapUiSettings &&
            isRotateGesturesEnabled == other.isRotateGesturesEnabled &&
            isScrollGesturesEnabled == other.isScrollGesturesEnabled &&
            isTiltGesturesEnabled == other.isTiltGesturesEnabled &&
            isZoomGesturesEnabled == other.isZoomGesturesEnabled &&
            isZoomEnabled == other.isZoomEnabled &&
            isCompassEnabled == other.isCompassEnabled &&
            myLocationButtonEnabled == other.myLocationButtonEnabled &&
            isScaleControlsEnabled == other.isScaleControlsEnabled

    override fun hashCode(): Int = Objects.hash(
        isRotateGesturesEnabled,
        isScrollGesturesEnabled,
        isTiltGesturesEnabled,
        isZoomGesturesEnabled,
        isZoomEnabled,
        isCompassEnabled,
        myLocationButtonEnabled,
        isScaleControlsEnabled
    )

    fun copy(
        isRotateGesturesEnabled: Boolean = this.isRotateGesturesEnabled,
        isScrollGesturesEnabled: Boolean = this.isScrollGesturesEnabled,
        isTiltGesturesEnabled: Boolean = this.isTiltGesturesEnabled,
        isZoomGesturesEnabled: Boolean = this.isZoomGesturesEnabled,
        isZoomEnabled: Boolean = this.isZoomEnabled,
        isCompassEnabled: Boolean = this.isCompassEnabled,
        myLocationButtonEnabled: Boolean = this.myLocationButtonEnabled,
        isScaleControlsEnabled: Boolean = this.isScaleControlsEnabled
    ): MapUiSettings = MapUiSettings(
        isRotateGesturesEnabled = isRotateGesturesEnabled,
        isScrollGesturesEnabled = isScrollGesturesEnabled,
        isTiltGesturesEnabled = isTiltGesturesEnabled,
        isZoomGesturesEnabled = isZoomGesturesEnabled,
        isZoomEnabled = isZoomEnabled,
        isCompassEnabled = isCompassEnabled,
        myLocationButtonEnabled = myLocationButtonEnabled,
        isScaleControlsEnabled = isScaleControlsEnabled
    )

    override fun toString(): String {
        return "MapUiSettings(" +
                "isRotateGesturesEnabled=$isRotateGesturesEnabled, " +
                "isScrollGesturesEnabled=$isScrollGesturesEnabled," +
                "isTiltGesturesEnabled=$isTiltGesturesEnabled, " +
                "isZoomGesturesEnabled=$isZoomGesturesEnabled, " +
                "isZoomEnabled=$isZoomEnabled, " +
                "isCompassEnabled=$isCompassEnabled, " +
                "myLocationButtonEnabled=$myLocationButtonEnabled, " +
                "isScaleControlsEnabled=$isScaleControlsEnabled)"
    }


}

