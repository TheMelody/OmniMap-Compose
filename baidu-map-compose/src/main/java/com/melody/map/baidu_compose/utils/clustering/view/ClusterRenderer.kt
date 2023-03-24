/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.view

import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import com.melody.map.baidu_compose.utils.clustering.ClusterManager

/**
 * Renders clusters.
 */
internal interface ClusterRenderer<T : ClusterItem?> {
    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     * @param clusters the clusters to be displayed.
     */
    fun onClustersChanged(clusters: Set<Cluster<T?>?>?)
    fun setOnClusterClickListener(listener: ClusterManager.OnClusterClickListener<T?>?)
    fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<T?>?)
    fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<T?>?)
    fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<T?>?)

    /**
     * Called when the view is added.
     */
    fun onAdd()

    /**
     * Called when the view is removed.
     */
    fun onRemove()

    fun setMaxDistanceAtZoom(maxDistance:Int)
}