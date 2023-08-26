// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
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

package com.melody.map.baidu_compose

import android.content.Context
import androidx.compose.runtime.*
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MultiPointItem
import com.baidu.mapapi.map.Polyline
import com.melody.map.baidu_compose.adapter.ComposeInfoWindowAdapter
import com.melody.map.baidu_compose.extensions.getSnippetExt
import com.melody.map.baidu_compose.extensions.getTag
import com.melody.map.baidu_compose.extensions.getTitleExt
import com.melody.map.baidu_compose.overlay.ClusterOverlayNode
import com.melody.map.baidu_compose.overlay.DragState
import com.melody.map.baidu_compose.overlay.MarkerNode
import com.melody.map.baidu_compose.overlay.MultiPointOverlayNode
import com.melody.map.baidu_compose.overlay.PolylineNode
import com.melody.map.baidu_compose.overlay.RoutePlanOverlayNode
import com.melody.map.baidu_compose.utils.clustering.ClusterItem
import com.melody.map.baidu_compose.utils.fastFirstOrNull

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

internal class MapApplier(
    val map: BaiduMap,
    mapContext: Context,
) : AbstractApplier<MapNode>(MapNodeRoot) {
    private val decorations = mutableListOf<MapNode>()
    private var enableMultipleInfoWindow: Boolean = false
    private val infoWindowAdapter = ComposeInfoWindowAdapter(mapContext)

    init {
        attachClickListeners()
    }

    override fun onClear() {
        map.clear()
        decorations.forEach { it.onCleared() }
        decorations.clear()
    }

    override fun insertBottomUp(index: Int, instance: MapNode) {
        decorations.add(index, instance)
        instance.onAttached()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {
        // insertBottomUp is preferred
    }

    override fun move(from: Int, to: Int, count: Int) {
        decorations.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            decorations[index + it].onRemoved()
        }
        decorations.remove(index, count)
    }

    internal fun enableMultipleInfoWindow(enable: Boolean) {
        this.enableMultipleInfoWindow = enable
    }

    /**
     * 隐藏InfoWindow
     * @param fromClusterOverlay 是不是聚合点的Marker隐藏
     * @param marker Marker覆盖物
     */
    internal fun hideInfoWindow(fromClusterOverlay:Boolean = false, marker: Marker?) {
        if (this.enableMultipleInfoWindow) { // 多窗口模式
            // 只隐藏单个
            marker?.hideInfoWindow()
        } else {
            if(!fromClusterOverlay) {
                map.hideInfoWindow()
            } else {
                // 点聚合，只显示1个InfoWindow场景，处理缩放场景，需要同步随对应的Marker消失再去隐藏InfoWindow
                val infoWindowLatLng = map.allInfoWindows.getOrNull(0)?.position
                if (infoWindowLatLng != null
                    && (infoWindowLatLng.latitude == marker?.position?.latitude
                       && infoWindowLatLng.longitude == marker.position?.longitude)
                ) {
                    map.hideInfoWindow()
                }
            }
        }
    }

    /**
     * MarkerNode节点点击触发的InfoWindow显示
     */
    internal fun showInfoWindow(markerNode: MarkerNode?){
        val marker = markerNode?.marker ?: return
        if (markerNode.infoWindow != null) {
            val infoWindow = infoWindowAdapter.getInfoWindow(markerNode)
            if (this.enableMultipleInfoWindow) {
                marker.showInfoWindow(infoWindow)
            } else {
                // 使用BaiduMap会先隐藏其他已添加的InfoWindow, 再添加新的InfoWindow
                map.showInfoWindow(infoWindow)
            }
        } else {
            if (markerNode.infoContent != null
                || (marker.getTitleExt() != null || marker.getSnippetExt() != null || marker.getTag() != null)
            ) {
                val infoContents = infoWindowAdapter.getInfoContents(markerNode)
                if (this.enableMultipleInfoWindow) {
                    marker.showInfoWindow(infoContents)
                } else {
                    map.showInfoWindow(infoContents)
                }
            }
        }
    }

    /**
     * 聚合点的Marker点击显示InfoWindow
     */
    internal fun showInfoWindow(marker: Marker, clusterItem: ClusterItem?, clusterNode: ClusterOverlayNode?){
        if (clusterNode == null || clusterItem == null) return
        val infoWindow = infoWindowAdapter.getInfoWindow(clusterItem,clusterNode)
        if (this.enableMultipleInfoWindow) {
            marker.showInfoWindow(infoWindow)
        } else {
            // 使用BaiduMap会先隐藏其他已添加的InfoWindow, 再添加新的InfoWindow
            map.showInfoWindow(infoWindow)
        }
    }

    private fun attachClickListeners() {
        // Marker的点击事件
        map.setOnMarkerClickListener { marker ->
            val markerNode = decorations.nodeForMarker(marker)
            showInfoWindow(markerNode)
            markerNode?.onMarkerClick?.invoke(marker) ?: false
        }
        // Polyline的点击事件
        map.setOnPolylineClickListener {
            decorations.nodeForPolyline(it)?.onPolylineClick?.invoke(it)
                ?: decorations.nodeForRoutePlanPolyline(it)?.onPolylineClick?.invoke(it) ?: false
        }
        // 长按触发
        map.setOnMarkerDragListener(object : BaiduMap.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker) {
                with(decorations.nodeForMarker(marker)) {
                    this?.markerState?.position = marker.position
                    this?.markerState?.dragState = DragState.DRAG
                }
            }
            override fun onMarkerDragEnd(marker: Marker) {
                with(decorations.nodeForMarker(marker)) {
                    this?.markerState?.position = marker.position
                    this?.markerState?.dragState = DragState.END
                }
            }
            override fun onMarkerDragStart(marker: Marker) {
                with(decorations.nodeForMarker(marker)) {
                    this?.markerState?.position = marker.position
                    this?.markerState?.dragState = DragState.START
                }
            }
        })
        // MultiPointOverlay的点击事件
        map.setOnMultiPointClickListener { _, multiPointItem ->
            val node = decorations.nodeForMultiPoint(multiPointItem)
            if(null != node) {
                node.onPointItemClick.invoke(multiPointItem)
                return@setOnMultiPointClickListener true
            }
            return@setOnMultiPointClickListener false
        }
    }
}
/**
 * Marker
 */
private fun MutableList<MapNode>.nodeForMarker(marker: Marker): MarkerNode? =
    fastFirstOrNull { it is MarkerNode && it.marker.extraInfo == marker.extraInfo } as? MarkerNode

/**
 * Polyline
 */
private fun MutableList<MapNode>.nodeForPolyline(polyline: Polyline): PolylineNode? =
    fastFirstOrNull { it is PolylineNode && it.polyline == polyline } as? PolylineNode

/**
 * MultiPointOverlay
 */
private fun MutableList<MapNode>.nodeForMultiPoint(multiPointItem: MultiPointItem): MultiPointOverlayNode? =
    fastFirstOrNull { it is MultiPointOverlayNode && null != it.multiPointOverlay.multiPointItems.fastFirstOrNull { child -> child == multiPointItem } } as? MultiPointOverlayNode


/**
 * RoutePlanOverlay
 */
private fun MutableList<MapNode>.nodeForRoutePlanPolyline(polyline: Polyline): RoutePlanOverlayNode? =
    fastFirstOrNull { it is RoutePlanOverlayNode && null != it.routePlanOverlay?.getAllOverlayList()?.fastFirstOrNull { child -> child == polyline } } as? RoutePlanOverlayNode
