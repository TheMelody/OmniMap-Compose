/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering

import com.baidu.mapapi.model.LatLng

/**
 * A collection of ClusterItems that are nearby each other.
 */
interface Cluster<T : ClusterItem?> {
    val position: LatLng?
    val items: Collection<T>
    val size: Int
}