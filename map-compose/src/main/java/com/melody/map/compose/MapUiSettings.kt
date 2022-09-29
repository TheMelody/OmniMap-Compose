package com.melody.map.compose

import java.util.*

internal val DefaultMapUiSettings = MapUiSettings()

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

