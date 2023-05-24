/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.algo

import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import com.melody.map.baidu_compose.utils.clustering.projection.Bounds
import com.melody.map.baidu_compose.utils.clustering.projection.Point
import com.melody.map.baidu_compose.utils.clustering.projection.SphericalMercatorProjection
import com.melody.map.baidu_compose.utils.clustering.quadtree.PointQuadTree
import kotlin.math.pow

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 *
 *
 * High level algorithm:<br></br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br></br>
 * 2. Create a cluster with the center of the item. <br></br>
 * 3. Add all items that are within a certain distance to the cluster. <br></br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br></br>
 * 5. Remove those items from the list of candidate clusters.
 *
 *
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
internal class NonHierarchicalDistanceBasedAlgorithm<T : ClusterItem?> : Algorithm<T> {
    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val mItems: MutableCollection<QuadItem<T>> = ArrayList()
    private var mMaxDistanceAtZoom: Int = 100

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val mQuadTree = PointQuadTree<QuadItem<T>>(0.0, 1.0, 0.0, 1.0)
    override fun addItem(item: T) {
        val quadItem = QuadItem(item)
        synchronized(mQuadTree) {
            mItems.add(quadItem)
            mQuadTree.add(quadItem)
        }
    }

    override fun addItems(items: Collection<T?>) {
        for (item in items) {
            if (item != null) {
                addItem(item)
            }
        }
    }

    override fun clearItems() {
        synchronized(mQuadTree) {
            mItems.clear()
            mQuadTree.clear()
        }
    }

    override fun removeItem(item: T) {
        // TODO: delegate QuadItem#hashCode and QuadItem#equals to its item.
        throw UnsupportedOperationException("NonHierarchicalDistanceBasedAlgorithm.remove not implemented")
    }

    override fun setMaxDistanceAtZoom(maxDistance:Int) {
        this.mMaxDistanceAtZoom = maxDistance
    }

    /**
     * cluster算法核心
     * @param zoom map的级别
     * @return
     */
    override fun getClusters(zoom: Double?): Set<Cluster<T>?> {
        val discreteZoom = zoom?.toInt()?:0
        val zoomSpecificSpan = this.mMaxDistanceAtZoom / 2.0.pow(discreteZoom.toDouble()) / 256
        val visitedCandidates: MutableSet<QuadItem<T>> = HashSet()
        val results: MutableSet<Cluster<T>?> = HashSet()
        val distanceToCluster: MutableMap<QuadItem<T>, Double> = HashMap()
        val itemToCluster: MutableMap<QuadItem<T>, StaticCluster<T>> = HashMap()
        synchronized(mQuadTree) {
            for (candidate in mItems) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }
                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                var clusterItems: Collection<QuadItem<T>>?
                // search 某边界范围内的clusterItems
                clusterItems = mQuadTree.search(searchBounds)
                if (clusterItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val cluster = StaticCluster<T>(candidate.clusterItem?.getPosition())
                results.add(cluster)
                for (clusterItem in clusterItems) {
                    val existingDistance = distanceToCluster[clusterItem]
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]?.remove(clusterItem.clusterItem)
                    }
                    distanceToCluster[clusterItem] = distance
                    cluster.add(clusterItem.clusterItem)
                    itemToCluster[clusterItem] = cluster
                }
                visitedCandidates.addAll(clusterItems)
            }
        }
        return results
    }

    override val items: Collection<T>
        get() {
            val items: MutableList<T> = ArrayList()
            synchronized(mQuadTree) {
                for (quadItem in mItems) {
                    items.add(quadItem.clusterItem)
                }
            }
            return items
        }

    private fun distanceSquared(a: Point?, b: Point?): Double {
        return (a!!.x - b!!.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }

    private fun createBoundsFromSpan(p: Point?, span: Double): Bounds {
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        val halfSpan = span / 2
        return Bounds(
            p!!.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan
        )
    }

    private class QuadItem<T : ClusterItem?>(val clusterItem: T) : PointQuadTree.Item, Cluster<T> {
        override val point: Point?
        override val position: LatLng? = clusterItem?.getPosition()
        private val singletonSet: Set<T>

        init {
            point = PROJECTION.toPoint(position)
            singletonSet = setOf(clusterItem)
        }

        override val items: Collection<T>
            get() = singletonSet

        override val size: Int
            get() = 1
    }

    companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}