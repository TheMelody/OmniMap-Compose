package com.melody.map.gd_compose.poperties

import java.util.*

internal val DefaultMapUiSettings = MapUiSettings()

/**
 * @param showMapLogo 是否显示地图logo
 * @param isRotateGesturesEnabled 旋转手势是否可用
 * @param isScrollGesturesEnabled 拖拽手势是否可用
 * @param isTiltGesturesEnabled 倾斜手势是否可用
 * @param isZoomGesturesEnabled 缩放手势是否可用
 * @param isZoomEnabled 缩放按钮是否可见
 * @param isCompassEnabled 指南针控件是否可见
 * @param myLocationButtonEnabled 设置默认定位按钮是否显示，非必需设置。
 * @param isScaleControlsEnabled 比例尺控件是否可见，【注意】：如果想用isScaleControlsEnabled的话，showMapLogo必须为true
 */
class MapUiSettings(
    val showMapLogo: Boolean = true,
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
            showMapLogo == other.showMapLogo &&
            isRotateGesturesEnabled == other.isRotateGesturesEnabled &&
            isScrollGesturesEnabled == other.isScrollGesturesEnabled &&
            isTiltGesturesEnabled == other.isTiltGesturesEnabled &&
            isZoomGesturesEnabled == other.isZoomGesturesEnabled &&
            isZoomEnabled == other.isZoomEnabled &&
            isCompassEnabled == other.isCompassEnabled &&
            myLocationButtonEnabled == other.myLocationButtonEnabled &&
            isScaleControlsEnabled == other.isScaleControlsEnabled

    override fun hashCode(): Int = Objects.hash(
        showMapLogo,
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
        showMapLogo: Boolean = this.showMapLogo,
        isRotateGesturesEnabled: Boolean = this.isRotateGesturesEnabled,
        isScrollGesturesEnabled: Boolean = this.isScrollGesturesEnabled,
        isTiltGesturesEnabled: Boolean = this.isTiltGesturesEnabled,
        isZoomGesturesEnabled: Boolean = this.isZoomGesturesEnabled,
        isZoomEnabled: Boolean = this.isZoomEnabled,
        isCompassEnabled: Boolean = this.isCompassEnabled,
        myLocationButtonEnabled: Boolean = this.myLocationButtonEnabled,
        isScaleControlsEnabled: Boolean = this.isScaleControlsEnabled
    ): MapUiSettings = MapUiSettings(
        showMapLogo = showMapLogo,
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
                "showMapLogo=$showMapLogo, " +
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

