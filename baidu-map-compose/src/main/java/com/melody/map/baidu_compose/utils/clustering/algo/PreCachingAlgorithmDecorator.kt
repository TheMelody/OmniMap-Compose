/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.algo

import android.util.LruCache
import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Optimistically fetch clusters for adjacent zoom levels, caching them as necessary.
 */
internal class PreCachingAlgorithmDecorator<T : ClusterItem?>(private val mAlgorithm: Algorithm<T?>) :
    Algorithm<T?> {
    // TODO: evaluate maxSize parameter for LruCache.
    private val mCache = LruCache<Int, Set<Cluster<T?>?>?>(5)
    private val mCacheLock: ReadWriteLock = ReentrantReadWriteLock()
    override fun addItem(item: T?) {
        mAlgorithm.addItem(item)
        clearCache()
    }

    override fun addItems(items: Collection<T?>) {
        mAlgorithm.addItems(items)
        clearCache()
    }

    override fun clearItems() {
        mAlgorithm.clearItems()
        clearCache()
    }

    override fun setMaxDistanceAtZoom(maxDistance: Int) {
        mAlgorithm.setMaxDistanceAtZoom(maxDistance)
    }

    override fun removeItem(item: T?) {
        mAlgorithm.removeItem(item)
        clearCache()
    }

    private fun clearCache() {
        mCache.evictAll()
    }

    override fun getClusters(zoom: Double?): Set<Cluster<T?>?>? {
        val discreteZoom = zoom?.toInt()?:0
        val results = getClustersInternal(discreteZoom)
        // TODO: Check if requests are already in-flight.
        if (mCache[discreteZoom + 1] == null) {
            Thread(PrecacheRunnable(discreteZoom + 1)).start()
        }
        if (mCache[discreteZoom - 1] == null) {
            Thread(PrecacheRunnable(discreteZoom - 1)).start()
        }
        return results
    }

    override val items: Collection<T?>?
        get() = mAlgorithm.items

    private fun getClustersInternal(discreteZoom: Int): Set<Cluster<T?>?>? {
        var results: Set<Cluster<T?>?>?
        mCacheLock.readLock().lock()
        results = mCache[discreteZoom]
        mCacheLock.readLock().unlock()
        if (results == null) {
            mCacheLock.writeLock().lock()
            results = mCache[discreteZoom]
            if (results == null) {
                results = mAlgorithm.getClusters(discreteZoom.toDouble())
                mCache.put(discreteZoom, results)
            }
            mCacheLock.writeLock().unlock()
        }
        return results
    }

    private inner class PrecacheRunnable(private val mZoom: Int) : Runnable {
        override fun run() {
            try {
                // Wait between 500 - 1000 ms.
                Thread.sleep((Math.random() * 500 + 500).toLong())
            } catch (e: InterruptedException) {
                // ignore. keep going.
            }
            getClustersInternal(mZoom)
        }
    }
}