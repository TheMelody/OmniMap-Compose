/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.quadtree

import com.melody.map.baidu_compose.utils.clustering.projection.Bounds
import com.melody.map.baidu_compose.utils.clustering.projection.Point

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
internal class PointQuadTree<T : PointQuadTree.Item?> private constructor(
    /**
     * The bounds of this quad.
     */
    private val mBounds: Bounds,
    /**
     * The depth of this quad in the tree.
     */
    private val mDepth: Int
) {
    /**
     * The elements inside this quad, if any.
     */
    private var mItems: MutableList<T>? = null

    /**
     * Child quads.
     */
    private var mChildren: MutableList<PointQuadTree<T>>? = null

    /**
     * Creates a new quad tree with specified bounds.
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    constructor(minX: Double, maxX: Double, minY: Double, maxY: Double) : this(
        Bounds(
            minX,
            maxX,
            minY,
            maxY
        )
    )

    constructor(bounds: Bounds) : this(bounds, 0) {}
    private constructor(minX: Double, maxX: Double, minY: Double, maxY: Double, depth: Int) : this(
        Bounds(minX, maxX, minY, maxY), depth
    )

    /**
     * Insert an item.
     */
    fun add(item: T) {
        val point: Point? = item!!.point
        if (mBounds.contains(point!!.x, point.y)) {
            insert(point.x, point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        if (mChildren != null) {
            // 四个区域进行控制
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    mChildren!!.get(0).insert(x, y, item)
                } else { // top right
                    mChildren!!.get(1).insert(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    mChildren!!.get(2).insert(x, y, item)
                } else {
                    mChildren!!.get(3).insert(x, y, item)
                }
            }
            return
        }
        if (mItems == null) {
            mItems = ArrayList()
        }
        mItems!!.add(item)
        if (mItems!!.size > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split()
        }
    }

    /**
     * Split this quad.
     */
    private fun split() {
        mChildren = ArrayList(4)
        mChildren!!.add(
            PointQuadTree(
                mBounds.minX,
                mBounds.midX,
                mBounds.minY,
                mBounds.midY,
                mDepth + 1
            )
        )
        mChildren!!.add(
            PointQuadTree(
                mBounds.midX,
                mBounds.maxX,
                mBounds.minY,
                mBounds.midY,
                mDepth + 1
            )
        )
        mChildren!!.add(
            PointQuadTree(
                mBounds.minX,
                mBounds.midX,
                mBounds.midY,
                mBounds.maxY,
                mDepth + 1
            )
        )
        mChildren!!.add(
            PointQuadTree(
                mBounds.midX,
                mBounds.maxX,
                mBounds.midY,
                mBounds.maxY,
                mDepth + 1
            )
        )
        val items: List<T>? = mItems
        mItems = null
        for (item: T in items!!) {
            // re-insert items into child quads.
            insert(item!!.point!!.x, item.point!!.y, item)
        }
    }

    /**
     * Remove the given item from the set.
     *
     * @return whether the item was removed.
     */
    fun remove(item: T): Boolean {
        val point: Point? = item!!.point
        if (mBounds.contains(point!!.x, point.y)) {
            return remove(point.x, point.y, item)
        } else {
            return false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        if (mChildren != null) {
            if (y < mBounds.midY) {
                if (x < mBounds.midX) { // top left
                    return mChildren!!.get(0).remove(x, y, item)
                } else { // top right
                    return mChildren!!.get(1).remove(x, y, item)
                }
            } else {
                if (x < mBounds.midX) { // bottom left
                    return mChildren!!.get(2).remove(x, y, item)
                } else {
                    return mChildren!!.get(3).remove(x, y, item)
                }
            }
        } else {
            return mItems!!.remove(item)
        }
    }

    /**
     * Removes all points from the quadTree
     */
    fun clear() {
        mChildren = null
        if (mItems != null) {
            mItems!!.clear()
        }
    }

    interface Item {
        val point: Point?
    }

    /**
     * Search for all items within a given bounds.
     */
    fun search(searchBounds: Bounds): Collection<T> {
        val results: MutableList<T> = ArrayList()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (!mBounds.intersects(searchBounds)) {
            return
        }
        if (mChildren != null) {
            for (quad: PointQuadTree<T> in mChildren!!) {
                quad.search(searchBounds, results)
            }
        } else if (mItems != null) {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mItems!!)
            } else {
                for (item: T in mItems!!) {
                    if (searchBounds.contains(item!!.point)) {
                        results.add(item)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Maximum number of elements to store in a quad before splitting.
         */
        private val MAX_ELEMENTS: Int = 50

        /**
         * Maximum depth.
         */
        private val MAX_DEPTH: Int = 40
    }
}