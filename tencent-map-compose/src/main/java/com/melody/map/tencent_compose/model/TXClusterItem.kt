package com.melody.map.tencent_compose.model

import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.vector.utils.clustering.ClusterItem

/**
 * ClusterItem在地图上表示是一个Marker覆盖物，这里继承ClusterItem，对外暴露【**修改**单个Marker覆盖物图标】的方法
 */
interface TXClusterItem: ClusterItem {
    /**
     * 单个点的图标
     */
    fun getIcon(): BitmapDescriptor?
}