/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering

import android.util.Log
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import java.util.Collections

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 *
 *
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
internal class MarkerManager(private val mMap: BaiduMap) : BaiduMap.OnMarkerClickListener,
    BaiduMap.OnMarkerDragListener {
    private val mNamedCollections: MutableMap<String, Collection?> = HashMap()
    private val mAllMarkers: MutableMap<Marker?, Collection> = HashMap()
    fun newCollection(): Collection {
        return Collection()
    }

    /**
     * Create a new named collection, which can later be looked up by [.getCollection]
     * @param id a unique id for this collection.
     */
    fun newCollection(id: String): Collection {
        if (mNamedCollections[id] != null) {
            throw IllegalArgumentException("collection id is not unique: $id")
        }
        val collection = Collection()
        mNamedCollections[id] = collection
        return collection
    }

    /**
     * Gets a named collection that was created by [.newCollection]
     * @param id the unique id for this collection.
     */
    fun getCollection(id: String): Collection? {
        return mNamedCollections.get(id)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val collection: Collection? = mAllMarkers.get(marker)
        return collection?.mMarkerClickListener?.onMarkerClick(marker)?:false
    }

    override fun onMarkerDragStart(marker: Marker) {
        val collection: Collection? = mAllMarkers.get(marker)
        collection?.mMarkerDragListener?.onMarkerDragStart(marker)
    }

    override fun onMarkerDrag(marker: Marker) {
        val collection: Collection? = mAllMarkers.get(marker)
        collection?.mMarkerDragListener?.onMarkerDrag(marker)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val collection: Collection? = mAllMarkers.get(marker)
        collection?.mMarkerDragListener?.onMarkerDragEnd(marker)
    }

    /**
     * Removes a marker from its collection.
     *
     * @param marker the marker to remove.
     * @return true if the marker was removed.
     */
    fun remove(marker: Marker?): Boolean {
        val collection: Collection? = mAllMarkers.get(marker)
        return collection != null && collection.remove(marker)
    }

    inner class Collection {
        private val mMarkers: MutableSet<Marker?> = HashSet()
        var mMarkerClickListener: BaiduMap.OnMarkerClickListener? = null
        var mMarkerDragListener: BaiduMap.OnMarkerDragListener? = null
        fun addMarker(opts: MarkerOptions?): Marker? {
            val marker: Marker? = mMap.addOverlay(opts) as? Marker?
            if(null != marker) {
                mMarkers.add(marker)
                mAllMarkers[marker] = this@Collection
            } else {
                Log.w("MarkerManager","Collection#addMarker，Exception Reason：Map.addOverlay(**) as Marker => return null，MarkerOptions：$opts")
            }
            return marker
        }

        fun remove(marker: Marker?): Boolean {
            if (mMarkers.remove(marker)) {
                mAllMarkers.remove(marker)
                marker?.remove()
                return true
            }
            return false
        }

        fun clear() {
            for (marker in mMarkers) {
                marker?.remove()
                mAllMarkers.remove(marker)
            }
            mMarkers.clear()
        }

        val markers: kotlin.collections.Collection<Marker?>
            get() {
                return Collections.unmodifiableCollection(mMarkers)
            }

        fun setOnMarkerClickListener(markerClickListener: BaiduMap.OnMarkerClickListener?) {
            mMarkerClickListener = markerClickListener
        }

        fun setOnMarkerDragListener(markerDragListener: BaiduMap.OnMarkerDragListener?) {
            mMarkerDragListener = markerDragListener
        }
    }
}