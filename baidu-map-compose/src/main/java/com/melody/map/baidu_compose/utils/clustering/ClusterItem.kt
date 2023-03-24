/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering

import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.model.LatLng

/**
 * ClusterItem在地图上表示是一个Marker覆盖物
 */
interface ClusterItem {
    /**
     * 单个Marker的位置
     */
    fun getPosition():LatLng?

    /**
     * 单个点的图标
     */
    fun getIcon():BitmapDescriptor?

    fun getInfoWindowYOffset(): Int
}