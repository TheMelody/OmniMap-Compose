package com.melody.map.myapplication.repo

import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.poperties.MapUiSettings

/**
 * MarkerAnimRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/21 15:47
 */
object MarkerAnimRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            showMapLogo = true,
            isScaleControlsEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }

    fun randomLatLngList(centerLatLng: LatLng): List<LatLng> {
        val latlngs = arrayOfNulls<LatLng>(500)
        for (i in 0..499) {
            val x = Math.random() * 0.5 - 0.25
            val y = Math.random() * 0.5 - 0.25
            latlngs[i] = LatLng(centerLatLng.latitude + x, centerLatLng.longitude + y)
        }
        return latlngs.asList().mapNotNull { it }
    }
}