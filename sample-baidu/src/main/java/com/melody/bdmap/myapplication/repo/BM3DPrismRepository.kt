package com.melody.bdmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.model.BM3DPrismDataModel
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings

/**
 * BM3DPrismRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 17:01
 */
object BM3DPrismRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isDoubleClickZoomEnabled = true,
            isZoomEnabled = true
        )
    }
    fun initMapProperties(): MapProperties {
        return MapProperties(isShowBuildings = false)
    }

    fun init3DPrismData(): BM3DPrismDataModel {
        val locations: MutableList<LatLng> = ArrayList()
        locations.add(LatLng(40.057777, 116.306951))
        locations.add(LatLng(40.057964, 116.307715))
        locations.add(LatLng(40.0559, 116.308631))
        locations.add(LatLng(40.0557, 116.307759))
        return BM3DPrismDataModel(
            sideFaceColor = Color(0xAAFF0000),
            topFaceColor = Color(0xAA00FF00),
            points = locations,
            customSideImage = null
        )
    }
}