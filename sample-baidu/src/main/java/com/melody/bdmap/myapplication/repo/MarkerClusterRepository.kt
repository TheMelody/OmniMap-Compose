package com.melody.bdmap.myapplication.repo

import android.graphics.BitmapFactory
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.melody.bdmap.myapplication.model.MapClusterItem
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils

/**
 * MarkerClusterRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/01 09:38
 */
object MarkerClusterRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomEnabled = true,
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isDoubleClickZoomEnabled = true
        )
    }

    private fun getClusterItemBitmap(): BitmapDescriptor? {
        val result = kotlin.runCatching {
            val assetsStream = SDKUtils.getApplicationContext().assets.open("red_marker.png")
            val bitmap = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeStream(assetsStream))
            assetsStream.close()
            bitmap
        }
        return result.getOrNull()
    }

    fun initClusterItems(): ArrayList<MapClusterItem> {
        val clusterItemIcon = getClusterItemBitmap()
        val items: ArrayList<MapClusterItem> = ArrayList()
        items.add(MapClusterItem(39.984059, 116.307621, clusterItemIcon))
        items.add(MapClusterItem(39.981954, 116.304703, clusterItemIcon))
        items.add(MapClusterItem(39.984355, 116.312256, clusterItemIcon))
        items.add(MapClusterItem(39.980442, 116.315346, clusterItemIcon))
        items.add(MapClusterItem(39.981527, 116.308994, clusterItemIcon))
        items.add(MapClusterItem(39.979751, 116.310539, clusterItemIcon))
        items.add(MapClusterItem(39.977252, 116.305776, clusterItemIcon))
        items.add(MapClusterItem(39.984026, 116.316419, clusterItemIcon))
        items.add(MapClusterItem(39.976956, 116.314874, clusterItemIcon))
        items.add(MapClusterItem(39.978501, 116.311827, clusterItemIcon))
        items.add(MapClusterItem(39.980277, 116.312814, clusterItemIcon))
        items.add(MapClusterItem(39.980236, 116.369022, clusterItemIcon))
        return items
    }
}