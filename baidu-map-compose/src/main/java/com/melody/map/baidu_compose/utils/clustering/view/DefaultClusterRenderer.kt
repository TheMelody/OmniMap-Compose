/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.melody.map.baidu_compose.utils.clustering.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.MessageQueue.IdleHandler
import android.util.SparseArray
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.Projection
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.MapApplier
import com.melody.map.baidu_compose.R
import com.melody.map.baidu_compose.overlay.ClusterOverlayNode
import com.melody.map.baidu_compose.utils.clustering.Cluster
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import com.melody.map.baidu_compose.utils.clustering.ClusterManager
import com.melody.map.baidu_compose.utils.clustering.MarkerManager
import com.melody.map.baidu_compose.utils.clustering.projection.Point
import com.melody.map.baidu_compose.utils.clustering.projection.SphericalMercatorProjection
import com.melody.map.baidu_compose.utils.clustering.ui.IconGenerator
import com.melody.map.baidu_compose.utils.clustering.ui.SquareTextView
import java.util.Collections
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
internal class DefaultClusterRenderer<T : ClusterItem?>(
    context: Context,
    private val mapApplier: MapApplier,
    clusterManager: ClusterManager<T>
) : ClusterRenderer<T> {

    private val mClusterManager: ClusterManager<T>
    private val mIconGenerator: IconGenerator = IconGenerator(context)
    private val mDensity: Float = context.resources.displayMetrics.density
    private var mClusterColor: Int = -1
    private var mMaxDistanceAtZoom: Int = 100
    private var mColoredCircleBackground: ShapeDrawable? = null
    private var mBuckets: IntArray = intArrayOf(10, 20, 50, 100, 200, 500, 1000)
    /**
     * If cluster size is less than this size, display individual markers.
     */
    private var mMinClusterSize = 4
    /**
     * Markers that are currently on the map.
     */
    private var mMarkers = Collections.newSetFromMap(ConcurrentHashMap<MarkerWithPosition, Boolean>())

    /**
     * Icons for each bucket.
     */
    private val mIcons = SparseArray<BitmapDescriptor?>()

    /**
     * Markers for single ClusterItems.
     */
    private val mMarkerCache = MarkerCache<T?>()

    /**
     * The currently displayed set of clusters.
     */
    private var mClusters: Set<Cluster<T?>?>? = null

    /**
     * Lookup between markers and the associated cluster.
     */
    private val mMarkerToCluster: MutableMap<Marker?, Cluster<T?>?> = HashMap()
    private val mClusterToMarker: MutableMap<Cluster<T?>?, Marker?> = HashMap()

    /**
     * The target zoom level for the current set of clusters.
     */
    private var mZoom = 0f
    private val mViewModifier: ViewModifier = ViewModifier()
    private var mClickListener: ClusterManager.OnClusterClickListener<T?>? = null
    private var mInfoWindowClickListener: ClusterManager.OnClusterInfoWindowClickListener<T?>? = null
    private var mItemClickListener: ClusterManager.OnClusterItemClickListener<T?>? = null
    private var mItemInfoWindowClickListener: ClusterManager.OnClusterItemInfoWindowClickListener<T?>? =
        null

    private var mClusterItemNode: ClusterOverlayNode? = null

    fun setClusterOverlayNode(clusterItemNode: ClusterOverlayNode) {
        this.mClusterItemNode = clusterItemNode
    }

    override fun onAdd() {
        mClusterManager.markerCollection?.setOnMarkerClickListener { marker ->
            mapApplier.showInfoWindow(marker, mMarkerCache[marker], mClusterItemNode)
            mItemClickListener?.onClusterItemClick(mMarkerCache[marker]) ?: false
        }
        mClusterManager.clusterMarkerCollection?.setOnMarkerClickListener { marker ->
            mClickListener != null && mClickListener?.onClusterClick(
                mMarkerToCluster[marker]
            )?:false
        }
    }

    override fun onRemove() {
        mClusterManager.markerCollection?.setOnMarkerClickListener(null)
        mClusterManager.clusterMarkerCollection?.setOnMarkerClickListener(null)
    }

    private fun makeClusterBackground(): LayerDrawable {
        mColoredCircleBackground = ShapeDrawable(OvalShape())
        val outline = ShapeDrawable(OvalShape())
        outline.paint.color = -0x7f000001 // Transparent white.
        val background = LayerDrawable(arrayOf<Drawable>(outline, mColoredCircleBackground!!))
        val strokeWidth = (mDensity * 3).toInt()
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth)
        return background
    }

    private fun makeSquareTextView(context: Context): SquareTextView {
        val squareTextView = SquareTextView(context)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        squareTextView.layoutParams = layoutParams
        val twelveDpi = (12 * mDensity).toInt()
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi)
        return squareTextView
    }

    private fun getColor(clusterSize: Int): Int {
        val hueRange = 220f
        val sizeRange = 300f
        val size = clusterSize.toFloat().coerceAtMost(sizeRange)
        val hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange
        return Color.HSVToColor(floatArrayOf(hue, 1f, .6f))
    }

    private fun getClusterText(bucket: Int): String {
        return if (bucket < mBuckets[0]) {
            bucket.toString()
        } else "$bucket+"
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    private fun getBucket(cluster: Cluster<T?>?): Int {
        val size = cluster?.size?:0
        if (size <= mBuckets[0]) {
            return size
        }
        for (i in 0 until (mBuckets.size - 1)) {
            if (size < mBuckets[i + 1]) {
                return mBuckets[i]
            }
        }
        return mBuckets[mBuckets.size - 1]
    }

    fun setBuckets(buckets: IntArray) {
        this.mBuckets = buckets
    }

    fun setMinClusterSize(minClusterSize: Int) {
        this.mMinClusterSize = minClusterSize
    }

    fun setClusterColor(clusterColor:Int) {
        this.mClusterColor = clusterColor
    }

    override fun setMaxDistanceAtZoom(maxDistance:Int) {
        this.mMaxDistanceAtZoom = maxDistance
    }

    private fun findClosestCluster(markers: List<Point?>?, point: Point?): Point? {
        if (markers.isNullOrEmpty()) {
            return null
        }
        // TODO: make this configurable.
        var minDistSquared: Double =
            (this.mMaxDistanceAtZoom * this.mMaxDistanceAtZoom).toDouble()
        var closest: Point? = null
        for (candidate: Point? in markers) {
            val dist = distanceSquared(candidate, point)
            if (dist < minDistSquared) {
                closest = candidate
                minDistSquared = dist
            }
        }
        return closest
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    @SuppressLint("HandlerLeak")
    private inner class ViewModifier : Handler(Looper.getMainLooper()) {
        private var mViewModificationInProgress = false
        private var mNextClusters: RenderTask? = null
        override fun handleMessage(msg: Message) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false
                if (mNextClusters != null) {
                    // Run the task that was queued up.
                    sendEmptyMessage(RUN_TASK)
                }
                return
            }
            removeMessages(RUN_TASK)
            if (mViewModificationInProgress) {
                // Busy - wait for the callback.
                return
            }
            if (mNextClusters == null) {
                // Nothing to do.
                return
            }
            var renderTask: RenderTask
            synchronized(this) {
                renderTask = mNextClusters as RenderTask
                mNextClusters = null
                mViewModificationInProgress = true
            }
            renderTask.setCallback { sendEmptyMessage(TASK_FINISHED) }
            renderTask.setProjection(mapApplier.map.projection)
            renderTask.setMapZoom(mapApplier.map.mapStatus.zoom)
            Thread(renderTask).start()
        }

        fun queue(clusters: Set<Cluster<T?>?>?) {
            synchronized(this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = RenderTask(clusters)
            }
            sendEmptyMessage(RUN_TASK)
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    internal fun shouldRenderAsCluster(cluster: Cluster<T?>?): Boolean {
        return (cluster?.size ?: 0) > mMinClusterSize
    }

    /**
     * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
     * new zoom level and set of clusters.
     *
     *
     * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
     * be executed by a MarkerModifier.
     *
     *
     * There are three stages for the render:
     *
     *
     * 1. Markers are added to the map
     *
     *
     * 2. Markers are animated to their final position
     *
     *
     * 3. Any old markers are removed from the map
     *
     *
     * When zooming in, markers are animated out from the nearest existing cluster. When zooming
     * out, existing clusters are animated to the nearest new cluster.
     */
    private inner class RenderTask(val clusters: Set<Cluster<T?>?>?) : Runnable {
        private var mCallback: Runnable? = null
        private var mProjection: Projection? = null
        private var mSphericalMercatorProjection: SphericalMercatorProjection? = null
        private var mapZoom = 0f

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
         */
        fun setCallback(callback: Runnable?) {
            mCallback = callback
        }

        fun setProjection(projection: Projection?) {
            mProjection = projection
        }

        fun setMapZoom(zoom: Float) {
            mapZoom = zoom
            mSphericalMercatorProjection =
                SphericalMercatorProjection(256 * 2.0.pow(zoom.coerceAtMost(mZoom).toDouble()))
        }

        override fun run() {
            if ((clusters == mClusters)) {
                mCallback?.run()
                return
            }
            val markerModifier = MarkerModifier()
            val zoom = mapZoom
            val zoomingIn = zoom > mZoom
            val zoomDelta = zoom - mZoom
            val markersToRemove = mMarkers
            val visibleBounds = mapApplier.map.mapStatus.bound
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            var existingClustersOnScreen: MutableList<Point?>? = null
            if (mClusters != null) {
                existingClustersOnScreen = ArrayList()
                for (c in mClusters!!) {
                    if (c != null) {
                        if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                            val point = mSphericalMercatorProjection!!.toPoint(c.position)
                            existingClustersOnScreen.add(point)
                        }
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            val newMarkers = Collections.newSetFromMap(
                ConcurrentHashMap<MarkerWithPosition, Boolean>()
            )
            if (clusters != null) {
                for (c in clusters) {
                    val onScreen = visibleBounds.contains(c?.position ?: LatLng(0.0,0.0))
                    if (zoomingIn && onScreen) {
                        val point = mSphericalMercatorProjection?.toPoint(c?.position ?: LatLng(0.0,0.0))
                        val closest = findClosestCluster(existingClustersOnScreen, point)
                        if (closest != null) {
                            val animateTo = mSphericalMercatorProjection?.toLatLng(closest)
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, animateTo))
                        } else {
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, null))
                        }
                    } else {
                        markerModifier.add(onScreen, CreateMarkerTask(c, newMarkers, null))
                    }
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree()

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers)

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            val newClustersOnScreen: MutableList<Point?> = ArrayList()
            if (clusters != null) {
                for (c in clusters) {
                    if (c != null) {
                        if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                            mSphericalMercatorProjection?.toPoint(c.position)?.let { newClustersOnScreen.add(it) }
                        }
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (marker: MarkerWithPosition in markersToRemove) {
                val onScreen = visibleBounds.contains(marker.position)
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && (zoomDelta > -3) && onScreen) {
                    val point = mSphericalMercatorProjection?.toPoint(marker.position)
                    val closest = findClosestCluster(newClustersOnScreen, point)
                    if (closest != null) {
                        val animateTo = mSphericalMercatorProjection?.toLatLng(closest)
                        markerModifier.animateThenRemove(marker, marker.position, animateTo)
                    } else {
                        markerModifier.remove(true, marker.marker)
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker)
                }
            }
            markerModifier.waitUntilFree()
            mMarkers = newMarkers
            mClusters = clusters
            mZoom = zoom
            mCallback?.run()
        }
    }

    override fun onClustersChanged(clusters: Set<Cluster<T?>?>?) {
        mViewModifier.queue(clusters)
    }

    override fun setOnClusterClickListener(listener: ClusterManager.OnClusterClickListener<T?>?) {
        mClickListener = listener
    }

    override fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<T?>?) {
        mInfoWindowClickListener = listener
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<T?>?) {
        mItemClickListener = listener
    }

    override fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<T?>?) {
        mItemInfoWindowClickListener = listener
    }

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
     * animating a markerWithPosition) is performed while trying not to block the rest of the app's
     * UI.
     */
    @SuppressLint("HandlerLeak")
    private inner class MarkerModifier : Handler(Looper.getMainLooper()),
        IdleHandler {
        private val lock: Lock = ReentrantLock()
        private val busyCondition = lock.newCondition()
        private val mCreateMarkerTasks: Queue<CreateMarkerTask> = LinkedList()
        private val mOnScreenCreateMarkerTasks: Queue<CreateMarkerTask> = LinkedList()
        private val mRemoveMarkerTasks: Queue<Marker> = LinkedList()
        private val mOnScreenRemoveMarkerTasks: Queue<Marker> = LinkedList()
        private val mAnimationTasks: Queue<AnimationTask> = LinkedList()

        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private var mListenerAdded = false

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        fun add(priority: Boolean, c: CreateMarkerTask) {
            lock.lock()
            sendEmptyMessage(BLANK)
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c)
            } else {
                mCreateMarkerTasks.add(c)
            }
            lock.unlock()
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        fun remove(priority: Boolean, m: Marker?) {
            lock.lock()
            sendEmptyMessage(BLANK)
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m)
            } else {
                mRemoveMarkerTasks.add(m)
            }
            lock.unlock()
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animate(marker: MarkerWithPosition?, from: LatLng?, to: LatLng?) {
            lock.lock()
            mAnimationTasks.add(AnimationTask(marker, from, to))
            lock.unlock()
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animateThenRemove(marker: MarkerWithPosition?, from: LatLng?, to: LatLng?) {
            lock.lock()
            val animationTask = AnimationTask(marker, from, to)
            animationTask.removeOnAnimationComplete(mClusterManager.markerManager)
            mAnimationTasks.add(animationTask)
            lock.unlock()
        }

        override fun handleMessage(msg: Message) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this)
                mListenerAdded = true
            }
            removeMessages(BLANK)
            lock.lock()
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (i in 0..9) {
                    performNextTask()
                }
                if (!isBusy) {
                    mListenerAdded = false
                    Looper.myQueue().removeIdleHandler(this)
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll()
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10)
                }
            } finally {
                lock.unlock()
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private fun performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll())
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll()?.perform()
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll()?.perform(this)
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll()?.perform(this)
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll())
            }
        }

        private fun removeMarker(marker: Marker?) {
            val cluster = mMarkerToCluster[marker]
            mClusterToMarker.remove(cluster)
            mMarkerCache.remove(marker)
            mMarkerToCluster.remove(marker)
            mClusterManager.markerManager.remove(marker)
        }

        /**
         * @return true if there is still work to be processed.
         */
        val isBusy: Boolean
            get() {
                try {
                    lock.lock()
                    return !((mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty()
                            && mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty()
                            && mAnimationTasks.isEmpty()))
                } finally {
                    lock.unlock()
                }
            }
        
        /**
         * Blocks the calling thread until all work has been processed.
         */
        fun waitUntilFree() {
            while (isBusy) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK)
                lock.lock()
                try {
                    if (isBusy) {
                        busyCondition.await()
                    }
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                } finally {
                    lock.unlock()
                }
            }
        }

        override fun queueIdle(): Boolean {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK)
            return true
        }
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    private class MarkerCache<T>() {
        private val mCache: MutableMap<T?, Marker> = HashMap()
        private val mCacheReverse: MutableMap<Marker, T> = HashMap()
        operator fun get(item: T?): Marker? {
            if(null == item) return null
            return mCache[item]
        }

        operator fun get(m: Marker?): T? {
            if(null == m) return null
            return mCacheReverse[m]
        }

        fun put(item: T, m: Marker) {
            mCache[item] = m
            mCacheReverse[m] = item
        }

        fun remove(m: Marker?) {
            if(null != m) {
                val item = mCacheReverse[m]
                mCacheReverse.remove(m)
                mCache.remove(item)
            }
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map.
     */
    internal fun onBeforeClusterItemRendered(item: T?, markerOptions: MarkerOptions?) {}

    /**
     * Called before the marker for a Cluster is added to the map.
     * The default implementation draws a circle with a rough count of the number of items.
     */
    internal fun onBeforeClusterRendered(cluster: Cluster<T?>?, markerOptions: MarkerOptions) {
        val bucket = getBucket(cluster)
        var descriptor = mIcons[bucket]
        if (descriptor == null) {
            mColoredCircleBackground?.paint?.color = if(mClusterColor == -1) getColor(bucket) else mClusterColor
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)))
            mIcons.put(bucket, descriptor)
        }
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        markerOptions.icon(descriptor)
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     */
    internal fun onClusterRendered(cluster: Cluster<T?>?, marker: Marker?) {}

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     */
    internal fun onClusterItemRendered(clusterItem: T?, marker: Marker?) {}

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    fun getMarker(clusterItem: T): Marker? {
        return mMarkerCache[clusterItem]
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    fun getMarker(cluster: Cluster<T?>): Marker? {
        return mClusterToMarker[cluster]
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    fun getClusterItem(marker: Marker?): T? {
        return mMarkerCache[marker]
    }

    /**
     * Get the Cluster from a marker
     *
     * @param marker which you will obtain its Cluster
     * @return a Cluster from a marker or null if it does not exists
     */
    fun getCluster(marker: Marker?): Cluster<T?>? {
        return mMarkerToCluster[marker]
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     * @param cluster            the cluster to render.
     * @param newMarkers a collection of markers to append any created markers.
     * @param animateFrom  the location to animate the markerWithPosition from, or null if no
     * animation is required.
     */
    private inner class CreateMarkerTask(
        private val cluster: Cluster<T?>?,
        private val newMarkers: MutableSet<MarkerWithPosition>,
        private val animateFrom: LatLng?
    ) {

        fun perform(markerModifier: MarkerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                for (item in cluster!!.items) {
                    var marker = mMarkerCache[item]
                    var markerWithPosition: MarkerWithPosition
                    if (marker == null) {
                        val markerOptions = MarkerOptions()
                        if (animateFrom != null) {
                            markerOptions.position(animateFrom)
                            markerOptions.icon(item?.getIcon())
                        } else {
                            markerOptions.position(item?.getPosition())
                            markerOptions.icon(item?.getIcon())
                        }
                        onBeforeClusterItemRendered(item, markerOptions)
                        marker = mClusterManager.markerCollection?.addMarker(markerOptions)
                        markerWithPosition = MarkerWithPosition(marker)
                        if (marker != null) {
                            mMarkerCache.put(item, marker)
                        }
                        if (animateFrom != null) {
                            markerModifier.animate(
                                markerWithPosition,
                                animateFrom,
                                item?.getPosition()
                            )
                        }
                    } else {
                        markerWithPosition = MarkerWithPosition(marker)
                    }
                    onClusterItemRendered(item, marker)
                    newMarkers.add(markerWithPosition)
                }
                return
            }
            val markerOptions = MarkerOptions()
                .position(animateFrom ?: cluster?.position?: LatLng(0.0,0.0))
            onBeforeClusterRendered(cluster, markerOptions)
            val marker = mClusterManager.clusterMarkerCollection?.addMarker(markerOptions)
            mMarkerToCluster[marker] = cluster
            mClusterToMarker[cluster] = marker
            val markerWithPosition = MarkerWithPosition(marker)
            if (animateFrom != null) {
                markerModifier.animate(markerWithPosition, animateFrom, cluster?.position)
            }
            onClusterRendered(cluster, marker)
            newMarkers.add(markerWithPosition)
        }
    }

    /**
     * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private class MarkerWithPosition(val marker: Marker?) {
        var position: LatLng? = marker?.position

        override fun equals(other: Any?): Boolean {
            return if (other is MarkerWithPosition) {
                (marker == other.marker)
            } else false
        }

        override fun hashCode(): Int {
            return marker.hashCode()
        }
    }

    init {
        mIconGenerator.setContentView(makeSquareTextView(context))
        mIconGenerator.setTextAppearance(R.style.BD_ClusterIcon_TextAppearance)
        mIconGenerator.setBackground(makeClusterBackground())
        mClusterManager = clusterManager
    }

    /**
     * Animates a markerWithPosition from one position to another
     */
    private inner class AnimationTask constructor(
        private val markerWithPosition: MarkerWithPosition?,
        from: LatLng?,
        to: LatLng?
    ) : AnimatorListenerAdapter(), AnimatorUpdateListener {
        private val marker: Marker? = markerWithPosition?.marker
        private val from: LatLng?
        private val to: LatLng?
        private var mRemoveOnComplete = false
        private var mMarkerManager: MarkerManager? = null

        init {
            this.from = from
            this.to = to
        }

        fun perform() {
            if (mRemoveOnComplete) {
                // 隐藏InfoWindow，没有触发动画前,Marker的position是正确的，动画完位置发生变化就无法匹配了
                mapApplier.hideInfoWindow(fromClusterOverlay = true, marker = marker)
            }
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.interpolator =
                ANIMATION_INTERP
            valueAnimator.addUpdateListener(this)
            valueAnimator.addListener(this)
            valueAnimator.start()
        }

        override fun onAnimationEnd(animation: Animator) {
            if (mRemoveOnComplete) {
                val cluster = mMarkerToCluster[marker]
                mClusterToMarker.remove(cluster)
                mMarkerCache.remove(marker)
                mMarkerToCluster.remove(marker)
                mMarkerManager?.remove(marker)
            }
            markerWithPosition?.position = to
        }

        fun removeOnAnimationComplete(markerManager: MarkerManager?) {
            mMarkerManager = markerManager
            mRemoveOnComplete = true
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            if(to != null && from != null) {
                val fraction = valueAnimator.animatedFraction
                val lat = (to.latitude - from.latitude) * fraction + from.latitude
                var lngDelta = to.longitude - from.longitude

                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + from.longitude
                val position = LatLng(lat, lng)
                marker?.position = position
            }
        }
    }

    companion object {
        private val BLANK = 0
        private val RUN_TASK = 0
        private val TASK_FINISHED = 1
        private fun distanceSquared(a: Point?, b: Point?): Double {
            return (a!!.x - b!!.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
        }
        private val ANIMATION_INTERP: TimeInterpolator = DecelerateInterpolator()
    }
}