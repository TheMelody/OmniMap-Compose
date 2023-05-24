package com.melody.tencentmap.myapplication.repo

import android.graphics.BitmapFactory
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.tencentmap.myapplication.model.MapClusterItem
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory

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
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }

    private fun getClusterItemBitmap(): BitmapDescriptor? {
        val result = kotlin.runCatching {
            val assetsStream = SDKUtils.getApplicationContext().assets.open("red_marker.png")
            // 腾讯的实现里面，fromBitmap，不能在地图创建之前使用
            val bitmap = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeStream(assetsStream))
            assetsStream.close()
            bitmap
        }
        return result.getOrNull()
    }

    fun initClusterItems(): ArrayList<MapClusterItem> {
        // 注意：腾讯的实现里面，fromBitmap，不能在地图创建之前使用，所以我们只能在地图加载完之后，调用
        val clusterItemIcon = getClusterItemBitmap()
        val items: ArrayList<MapClusterItem> = ArrayList()
        items.add(MapClusterItem(39.984059, 116.307621, clusterItemIcon,"可以自己定义对象，我自己控制里面的数据"))
        items.add(MapClusterItem(39.981954, 116.304703,  clusterItemIcon,"啊？"))
        items.add(MapClusterItem(39.984355, 116.312256,  clusterItemIcon,"哦？"))
        items.add(MapClusterItem(39.980442, 116.315346,  clusterItemIcon,"额？"))
        items.add(MapClusterItem(39.981527, 116.308994,  clusterItemIcon,"哈？"))
        items.add(MapClusterItem(39.979751, 116.310539,  clusterItemIcon,"嗯？"))
        items.add(MapClusterItem(39.977252, 116.305776,  clusterItemIcon,"切？"))
        items.add(MapClusterItem(39.984026, 116.316419,  clusterItemIcon,"呵？"))
        items.add(MapClusterItem(39.976956, 116.314874,  clusterItemIcon,"噗？"))
        items.add(MapClusterItem(39.978501, 116.311827,  clusterItemIcon,"嘻？"))
        items.add(MapClusterItem(39.980277, 116.312814,  clusterItemIcon,"噶？"))
        items.add(MapClusterItem(39.980236, 116.369022,  clusterItemIcon,this))
        return items
    }
}