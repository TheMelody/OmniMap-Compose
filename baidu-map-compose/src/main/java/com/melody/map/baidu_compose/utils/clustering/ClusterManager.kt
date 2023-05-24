/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering

import android.content.Context
import android.os.AsyncTask
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.Marker
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.utils.clustering.algo.Algorithm
import com.melody.map.baidu_compose.utils.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.melody.map.baidu_compose.utils.clustering.algo.PreCachingAlgorithmDecorator
import com.melody.map.baidu_compose.utils.clustering.view.ClusterRenderer
import com.melody.map.baidu_compose.utils.clustering.view.DefaultClusterRenderer
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Groups many items on a map based on zoom level.
 *
 *
 * ClusterManager should be added to the map
 *
 */
internal class ClusterManager<T : ClusterItem?> @JvmOverloads constructor(
    private val context: Context,
    private val mapApplier: MapApplier,
    val markerManager: MarkerManager = MarkerManager(mapApplier.map)
) : OnMapStatusChangeListener, BaiduMap.OnMarkerClickListener {
    val markerCollection: MarkerManager.Collection?
    val clusterMarkerCollection: MarkerManager.Collection?
    private var mAlgorithm: Algorithm<T?>?
    private val mAlgorithmLock: ReadWriteLock = ReentrantReadWriteLock()
    private var mRenderer: ClusterRenderer<T>
    private var mPreviousCameraPosition: MapStatus? = null
    private var mClusterTask: ClusterTask
    private val mClusterTaskLock: ReadWriteLock = ReentrantReadWriteLock()
    private var mOnClusterItemClickListener: OnClusterItemClickListener<T?>? = null
    private var mOnClusterInfoWindowClickListener: OnClusterInfoWindowClickListener<T?>? = null
    private var mOnClusterItemInfoWindowClickListener: OnClusterItemInfoWindowClickListener<T?>? =
        null
    private var mOnClusterClickListener: OnClusterClickListener<T?>? = null

    init {
        clusterMarkerCollection = markerManager.newCollection()
        markerCollection = markerManager.newCollection()
        mRenderer = DefaultClusterRenderer(context, mapApplier, this)
        mAlgorithm = PreCachingAlgorithmDecorator(NonHierarchicalDistanceBasedAlgorithm())
        mClusterTask = ClusterTask()
        mRenderer.onAdd()
    }

    fun setMaxDistanceAtZoom(distance: Float) {
        // 以dp为单位转px
        val maxDistanceAtZoom = (this.context.resources.displayMetrics.density * distance + 0.5f).toInt()
        mRenderer.setMaxDistanceAtZoom(maxDistanceAtZoom)
        mAlgorithm?.setMaxDistanceAtZoom(maxDistanceAtZoom)
    }

    fun setRenderer(view: ClusterRenderer<T>) {
        mRenderer.setOnClusterClickListener(null)
        mRenderer.setOnClusterItemClickListener(null)
        clusterMarkerCollection?.clear()
        markerCollection?.clear()
        mRenderer.onRemove()
        mRenderer = view
        mRenderer.onAdd()
        mRenderer.setOnClusterClickListener(mOnClusterClickListener)
        mRenderer.setOnClusterInfoWindowClickListener(mOnClusterInfoWindowClickListener)
        mRenderer.setOnClusterItemClickListener(mOnClusterItemClickListener)
        mRenderer.setOnClusterItemInfoWindowClickListener(mOnClusterItemInfoWindowClickListener)
    }

    fun setAlgorithm(algorithm: Algorithm<T?>) {
        mAlgorithmLock.writeLock().lock()
        try {
            mAlgorithm?.items?.let { algorithm.addItems(it) }
            mAlgorithm = PreCachingAlgorithmDecorator(algorithm)
        } finally {
            mAlgorithmLock.writeLock().unlock()
        }
        cluster()
    }

    fun clearItems() {
        mAlgorithmLock.writeLock().lock()
        try {
            mAlgorithm?.clearItems()
        } finally {
            mAlgorithmLock.writeLock().unlock()
        }
    }

    fun addItems(items: Collection<T?>?) {
        mAlgorithmLock.writeLock().lock()
        try {
            items?.let { mAlgorithm?.addItems(it) }
        } finally {
            mAlgorithmLock.writeLock().unlock()
        }
        cluster()
    }

    fun addItem(myItem: T) {
        mAlgorithmLock.writeLock().lock()
        try {
            mAlgorithm?.addItem(myItem)
        } finally {
            mAlgorithmLock.writeLock().unlock()
        }
    }

    fun removeItem(item: T) {
        mAlgorithmLock.writeLock().lock()
        try {
            mAlgorithm?.removeItem(item)
        } finally {
            mAlgorithmLock.writeLock().unlock()
        }
    }

    /**
     * Force a re-cluster. You may want to call this after adding new item(s).
     */
    fun cluster() {
        mClusterTaskLock.writeLock().lock()
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true)
            mClusterTask = ClusterTask()
            mClusterTask.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                mapApplier.map.mapStatus.zoom
            )
        } finally {
            mClusterTaskLock.writeLock().unlock()
        }
    }

    override fun onMapStatusChangeStart(mapStatus: MapStatus) {}
    override fun onMapStatusChangeStart(status: MapStatus, reason: Int) {}
    override fun onMapStatusChange(mapStatus: MapStatus) {
        if (mRenderer is OnMapStatusChangeListener) {
            (mRenderer as OnMapStatusChangeListener).onMapStatusChange(mapStatus)
        }

        // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        val position: MapStatus = mapApplier.map.mapStatus
        if (mPreviousCameraPosition != null && mPreviousCameraPosition!!.zoom == position.zoom) {
            return
        }
        mPreviousCameraPosition = mapApplier.map.mapStatus
        cluster()
    }

    override fun onMapStatusChangeFinish(mapStatus: MapStatus) {}
    override fun onMarkerClick(marker: Marker): Boolean {
        return markerManager.onMarkerClick(marker)
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come back.
     */
    private inner class ClusterTask : AsyncTask<Float?, Void?, Set<Cluster<T?>?>?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Float?): Set<Cluster<T?>?>? {
            mAlgorithmLock.readLock().lock()
            try {
                return mAlgorithm?.getClusters(params.getOrNull(0)?.toDouble())
            } finally {
                mAlgorithmLock.readLock().unlock()
            }
        }
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(clusters: Set<Cluster<T?>?>?) {
            mRenderer.onClustersChanged(clusters)
        }
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     */
    fun setOnClusterClickListener(listener: OnClusterClickListener<T?>?) {
        mOnClusterClickListener = listener
        mRenderer.setOnClusterClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     */
    fun setOnClusterInfoWindowClickListener(listener: OnClusterInfoWindowClickListener<T?>?) {
        mOnClusterInfoWindowClickListener = listener
        mRenderer.setOnClusterInfoWindowClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     */
    fun setOnClusterItemClickListener(listener: OnClusterItemClickListener<T?>?) {
        mOnClusterItemClickListener = listener
        mRenderer.setOnClusterItemClickListener(listener)
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     */
    fun setOnClusterItemInfoWindowClickListener(listener: OnClusterItemInfoWindowClickListener<T?>?) {
        mOnClusterItemInfoWindowClickListener = listener
        mRenderer.setOnClusterItemInfoWindowClickListener(listener)
    }

    /**
     * Called when a Cluster is clicked.
     */
    interface OnClusterClickListener<T : ClusterItem?> {
        fun onClusterClick(cluster: Cluster<T?>?): Boolean
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    interface OnClusterInfoWindowClickListener<T : ClusterItem?> {
        fun onClusterInfoWindowClick(cluster: Cluster<T>?)
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    interface OnClusterItemClickListener<T : ClusterItem?> {
        fun onClusterItemClick(item: T?): Boolean
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    interface OnClusterItemInfoWindowClickListener<T : ClusterItem?> {
        fun onClusterItemInfoWindowClick(item: T)
    }
}