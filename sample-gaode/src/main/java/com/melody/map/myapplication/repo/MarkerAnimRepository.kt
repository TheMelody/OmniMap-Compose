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

    /**
     * 这里仅测试使用，如真的有很多点，可以考虑用：点聚合效果，处理Marker过多的问题
     * 因为点太多，做动画，导致界面上Marker压盖、性能变差。
     */
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