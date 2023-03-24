// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.map.gd_compose.kernel

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.LruCache
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.AMap.OnMarkerClickListener
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.animation.AlphaAnimation
import com.amap.api.maps.model.animation.Animation
import com.melody.map.gd_compose.model.Cluster
import com.melody.map.gd_compose.model.ClusterClickListener
import com.melody.map.gd_compose.model.ClusterItem
import com.melody.map.gd_compose.model.ClusterRender

/**
 * 全部取自【高德点聚合】示例代码里面的ClusterOverlay
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/24 10:15
 */
internal class KernelClusterOverlay(
    private val context: Context,
    private val aMap: AMap,
    // 聚合范围的大小（指点像素单位距离内的点会聚合到一个点显示）
    private val clusterRadius: Int,
    // 没有设置render显示的图标
    private val defaultClusterIcon: BitmapDescriptor,
    defaultCacheSize: Int = 80
): OnCameraChangeListener, OnMarkerClickListener {
    private var mIsCanceled = false
    private val mClusters: MutableList<Cluster> = mutableListOf()
    private val mClusterItems: MutableList<ClusterItem> = mutableListOf()
    private var mLruCache: LruCache<Int, BitmapDescriptor>? = null
    private var mPXInMeters: Float = aMap.scalePerPixel
    private var mClusterDistance: Float = mPXInMeters * clusterRadius
    private val mMarkerHandlerThread = HandlerThread("addMarker")
    private val mSignClusterThread = HandlerThread("calculateCluster")
    private var mMarkerHandler: Handler? = null
    private var mSignClusterHandler: Handler? = null
    private val mAddMarkers: MutableList<Marker> = mutableListOf()
    private var mClusterClickListener: ClusterClickListener? = null
    private var mClusterRender: ClusterRender? = null

    init {
        //默认最多会缓存80张图片作为聚合显示元素图片,根据自己显示需求和app使用内存情况,可以修改数量
        mLruCache = object : LruCache<Int, BitmapDescriptor>(defaultCacheSize) { }
        aMap.setOnCameraChangeListener(this)
        aMap.setOnMarkerClickListener(this)
        initThreadHandler()
    }

    /**
     * 初始化Handler
     */
    private fun initThreadHandler() {
        mMarkerHandlerThread.start()
        mSignClusterThread.start()
        mMarkerHandler = MarkerHandler(mMarkerHandlerThread.looper)
        mSignClusterHandler = SignClusterHandler(mSignClusterThread.looper)
    }

    /**
     * 对点进行聚合
     */
    private fun assignClusters() {
        mIsCanceled = true
        mSignClusterHandler?.removeMessages(CALCULATE_CLUSTER)
        mSignClusterHandler?.sendEmptyMessage(CALCULATE_CLUSTER)
    }

    fun setClusterItems(clusterItems: List<ClusterItem>) {
        mSignClusterHandler?.removeMessages(INIT_CLUSTER_DATA_LIST)
        val message = Message.obtain()
        message.what = INIT_CLUSTER_DATA_LIST
        message.obj = clusterItems
        if(mIsCanceled){
            return
        }
        mSignClusterHandler?.sendMessageDelayed(message, 50)
    }

    /**
     * 设置聚合点的点击事件
     */
    fun setOnClusterClickListener(clusterClickListener: ClusterClickListener) {
        mClusterClickListener = clusterClickListener
    }

    /**
     * 设置聚合元素的渲染样式，不设置则默认为气泡加数字形式进行渲染
     */
    fun setClusterRenderer(render: ClusterRender?) {
        mClusterRender = render
    }

    override fun onCameraChange(p0: CameraPosition?) {
    }

    override fun onCameraChangeFinish(p0: CameraPosition?) {
        mPXInMeters = aMap.scalePerPixel
        mClusterDistance = mPXInMeters * clusterRadius
        assignClusters()
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        if (mClusterClickListener == null) {
            return true
        }
        val cluster = p0?.getObject() as? Cluster
        if (cluster != null) {
            mClusterClickListener?.onClick(p0, cluster.clusterItems)
            return true
        }
        return false
    }


    fun onDestroy() {
        mIsCanceled = true
        mSignClusterHandler?.removeCallbacksAndMessages(null)
        mMarkerHandler?.removeCallbacksAndMessages(null)
        mSignClusterThread.quit()
        mMarkerHandlerThread.quit()
        val iterator = mAddMarkers.iterator()
        while(iterator.hasNext()) {
            val marker = iterator.next()
            if(!marker.isRemoved) {
                marker.remove()
            }
            iterator.remove()
        }
        mLruCache?.evictAll()
    }

    /**
     * 将聚合元素添加至地图上
     */
    private fun addClusterToMap(clusters: List<Cluster>) {
        val removeMarkers = ArrayList<Marker>()
        removeMarkers.addAll(mAddMarkers)
        val alphaAnimation = AlphaAnimation(1f, 0f)
        val myAnimationListener = MyAnimationListener(removeMarkers)
        for (marker in removeMarkers) {
            marker.setAnimation(alphaAnimation)
            marker.setAnimationListener(myAnimationListener)
            marker.startAnimation()
        }
        for (cluster in clusters) {
            addSingleClusterToMap(cluster)
        }
    }

    private val mADDAnimation = AlphaAnimation(0f, 1f)

    /**
     * 将单个聚合元素添加至地图显示
     */
    private fun addSingleClusterToMap(cluster: Cluster) {
        val latLng: LatLng = cluster.centerLatLng
        val markerOptions = MarkerOptions()
        markerOptions.anchor(0.5f, 0.5f)
            .icon(getBitmapDes(cluster.clusterCount)).position(latLng)
        val marker: Marker = aMap.addMarker(markerOptions)
        marker.setAnimation(mADDAnimation)
        marker.setObject(cluster)
        marker.startAnimation()
        cluster.marker = marker
        mAddMarkers.add(marker)
    }


    private fun calculateClusters() {
        mIsCanceled = false
        mClusters.clear()
        val visibleBounds: LatLngBounds = aMap.projection.visibleRegion.latLngBounds
        for (clusterItem in mClusterItems) {
            if (mIsCanceled) {
                return
            }
            val latLng = clusterItem.getPosition()
            if (visibleBounds.contains(latLng)) {
                var cluster = getCluster(latLng, mClusters)
                if (cluster != null) {
                    cluster.addClusterItem(clusterItem)
                } else {
                    cluster = Cluster(latLng)
                    mClusters.add(cluster)
                    cluster.addClusterItem(clusterItem)
                }
            }
        }

        //复制一份数据，规避同步
        val clusters: MutableList<Cluster> = mutableListOf()
        clusters.addAll(mClusters)
        val message = Message.obtain()
        message.what = ADD_CLUSTER_LIST
        message.obj = clusters
        if (mIsCanceled) {
            return
        }
        mMarkerHandler?.sendMessage(message)
    }

    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     */
    private fun calculateSingleCluster(clusterItem: ClusterItem) {
        val visibleBounds: LatLngBounds = aMap.projection.visibleRegion.latLngBounds
        val latLng = clusterItem.getPosition()
        if (!visibleBounds.contains(latLng)) {
            return
        }
        var cluster = getCluster(latLng, mClusters)
        if (cluster != null) {
            cluster.addClusterItem(clusterItem)
            val message = Message.obtain()
            message.what = UPDATE_SINGLE_CLUSTER
            message.obj = cluster
            mMarkerHandler?.removeMessages(UPDATE_SINGLE_CLUSTER)
            mMarkerHandler?.sendMessageDelayed(message, 5)
        } else {
            cluster = Cluster(latLng)
            mClusters.add(cluster)
            cluster.addClusterItem(clusterItem)
            val message = Message.obtain()
            message.what = ADD_SINGLE_CLUSTER
            message.obj = cluster
            mMarkerHandler?.sendMessage(message)
        }
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     */
    private fun getCluster(latLng: LatLng, clusters: List<Cluster>): Cluster? {
        for (cluster in clusters) {
            val clusterCenterPoint: LatLng = cluster.centerLatLng
            val distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint).toDouble()
            if (distance < mClusterDistance && aMap.cameraPosition.zoom < 19) {
                return cluster
            }
        }
        return null
    }


    /**
     * 获取每个聚合点的绘制样式
     */
    private fun getBitmapDes(num: Int): BitmapDescriptor? {
        var bitmapDescriptor = mLruCache?.get(num)
        if (bitmapDescriptor == null) {
            val textView = TextView(context)
            if (num > 1) {
                textView.text = num.toString()
            }
            textView.gravity = Gravity.CENTER
            textView.setTextColor(Color.WHITE)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            if (mClusterRender != null && mClusterRender?.getDrawable(num) != null) {
                textView.background = mClusterRender?.getDrawable(num)
            } else {
                textView.background = BitmapDrawable(null, defaultClusterIcon.bitmap)
            }
            bitmapDescriptor = BitmapDescriptorFactory.fromView(textView)
            mLruCache?.put(num, bitmapDescriptor)
        }
        return bitmapDescriptor
    }

    /**
     * 更新已加入地图聚合点的样式
     */
    private fun updateCluster(cluster: Cluster) {
       cluster.marker?.setIcon(getBitmapDes(cluster.clusterCount))
    }


    //-----------------------辅助内部类用---------------------------------------------
    /**
     * marker渐变动画，动画结束后将Marker删除
     */
    internal class MyAnimationListener(private val mRemoveMarkers: MutableList<Marker>) :
        Animation.AnimationListener {
        override fun onAnimationStart() {}
        override fun onAnimationEnd() {
            val iterator = mRemoveMarkers.iterator()
            while(iterator.hasNext()) {
                val marker = iterator.next()
                if(!marker.isRemoved) {
                    marker.remove()
                }
                iterator.remove()
            }
        }
    }
    /**
     * 处理market添加，更新等操作
     */
    @Suppress("UNCHECKED_CAST")
    inner class MarkerHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            when (message.what) {
                ADD_CLUSTER_LIST -> {
                    addClusterToMap(message.obj as List<Cluster>)
                }
                ADD_SINGLE_CLUSTER -> {
                    addSingleClusterToMap(message.obj as Cluster)
                }

                UPDATE_SINGLE_CLUSTER -> {
                    updateCluster(message.obj as Cluster)
                }
            }
        }
    }

    /**
     * 处理聚合点算法线程
     */
    @Suppress("UNCHECKED_CAST")
    inner class SignClusterHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            when (message.what) {
                CALCULATE_CLUSTER -> calculateClusters()
                CALCULATE_SINGLE_CLUSTER -> {
                    val item = message.obj as ClusterItem
                    mClusterItems.add(item)
                    calculateSingleCluster(item)
                }
                INIT_CLUSTER_DATA_LIST -> {
                    mClusterItems.clear()
                    mClusterItems.addAll(message.obj as List<ClusterItem>)
                    assignClusters()
                }
            }
        }
    }

    companion object {
        const val ADD_CLUSTER_LIST = 0
        const val ADD_SINGLE_CLUSTER = 1
        const val UPDATE_SINGLE_CLUSTER = 2
        const val CALCULATE_CLUSTER = 3
        const val CALCULATE_SINGLE_CLUSTER = 4
        const val INIT_CLUSTER_DATA_LIST = 5
    }

}