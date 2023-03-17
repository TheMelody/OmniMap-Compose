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

import androidx.compose.runtime.*
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.Polyline
import com.melody.map.baidu_compose.adapter.ComposeInfoWindowAdapter
import com.melody.map.baidu_compose.overlay.DragState
import com.melody.map.baidu_compose.overlay.MarkerNode
import com.melody.map.baidu_compose.overlay.PolylineNode
import com.melody.map.baidu_compose.utils.fastFirstOrNull

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

internal class MapApplier(
    val map: BaiduMap,
    val mapView: MapView,
) : AbstractApplier<MapNode>(MapNodeRoot) {

    private val decorations = mutableListOf<MapNode>()
    private val infoWindowAdapter = ComposeInfoWindowAdapter(mapView)

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

    internal fun getInfoContents(marker: Marker, markerNode: MarkerNode): InfoWindow {
        return infoWindowAdapter.getInfoContents(marker, markerNode)
    }

    internal fun getInfoWindow(marker: Marker, markerNode: MarkerNode): InfoWindow {
        return infoWindowAdapter.getInfoWindow(marker, markerNode)
    }

    private fun attachClickListeners() {
        // 设置Marker的点击事件，return true拦截
        map.setOnMarkerClickListener { marker ->
            val markerNode = decorations.nodeForMarker(marker)
            val flag = markerNode?.onMarkerClick?.invoke(marker) ?: false
            if(null != markerNode) {
                if (markerNode.infoWindow != null) {
                    map.showInfoWindow(getInfoWindow(marker,markerNode))
                } else {
                    map.showInfoWindow(getInfoContents(marker,markerNode))
                }
            }
            flag
        }
        // Polyline的点击事件
        map.setOnPolylineClickListener {
            decorations.nodeForPolyline(it)?.onPolylineClick?.invoke(it) ?: false
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
        /*
        // MultiPointOverlay的点击事件
        map.setOnMultiPointClickListener { multiPointItem ->
            val node = decorations.nodeForMultiPoint(multiPointItem)
            if(null != node) {
                node.onPointItemClick.invoke(multiPointItem)
                return@setOnMultiPointClickListener true
            }
            return@setOnMultiPointClickListener false
        }*/
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
