/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.algo

import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem

/**
 * A cluster whose center is determined upon creation.
 */
internal class StaticCluster<T : ClusterItem?>(override val position: LatLng?) : Cluster<T> {
    private val mItems: MutableList<T> = ArrayList()
    fun add(t: T): Boolean {
        return mItems.add(t)
    }

    fun remove(t: T): Boolean {
        return mItems.remove(t)
    }

    override val items: Collection<T>
        get() = mItems
    override val size: Int
        get() = mItems.size

    override fun toString(): String {
        return ("StaticCluster{"
                + "mCenter=" + position
                + ", mItems.size=" + mItems.size
                + '}')
    }
}