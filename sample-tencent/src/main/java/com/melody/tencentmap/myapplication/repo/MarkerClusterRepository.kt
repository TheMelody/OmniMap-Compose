package com.melody.tencentmap.myapplication.repo

import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.tencentmap.myapplication.model.MapClusterItem

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

    fun initClusterItems(): ArrayList<MapClusterItem> {
        val items: ArrayList<MapClusterItem> = ArrayList()
        items.add(MapClusterItem(39.984059, 116.307621, "可以自己定义对象，我自己控制里面的数据"))
        items.add(MapClusterItem(39.981954, 116.304703, "啊？"))
        items.add(MapClusterItem(39.984355, 116.312256, "哦？"))
        items.add(MapClusterItem(39.980442, 116.315346, "额？"))
        items.add(MapClusterItem(39.981527, 116.308994, "哈？"))
        items.add(MapClusterItem(39.979751, 116.310539, "嗯？"))
        items.add(MapClusterItem(39.977252, 116.305776, "切？"))
        items.add(MapClusterItem(39.984026, 116.316419, "呵？"))
        items.add(MapClusterItem(39.976956, 116.314874, "噗？"))
        items.add(MapClusterItem(39.978501, 116.311827, "嘻？"))
        items.add(MapClusterItem(39.980277, 116.312814, "噶？"))
        items.add(MapClusterItem(39.980236, 116.369022, this))
        return items
    }
}