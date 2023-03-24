/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.algo

import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem

/**
 * Logic for computing clusters
 */
internal interface Algorithm<T : ClusterItem?> {
    fun addItem(item: T)
    fun addItems(items: Collection<T?>)
    fun clearItems()
    fun removeItem(item: T)
    fun setMaxDistanceAtZoom(maxDistance:Int)
    fun getClusters(zoom: Double?): Set<Cluster<T>?>?
    val items: Collection<T>?
}