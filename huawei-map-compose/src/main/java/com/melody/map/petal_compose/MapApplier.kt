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

package com.melody.map.petal_compose

import androidx.compose.runtime.*
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.model.Circle
import com.huawei.hms.maps.model.GroundOverlay
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.Polygon
import com.huawei.hms.maps.model.Polyline
import com.melody.map.petal_compose.adapter.ComposeInfoWindowAdapter
import com.melody.map.petal_compose.overlay.CircleNode
import com.melody.map.petal_compose.overlay.DragState
import com.melody.map.petal_compose.overlay.GroundOverlayNode
import com.melody.map.petal_compose.overlay.MarkerNode
import com.melody.map.petal_compose.overlay.PolygonNode
import com.melody.map.petal_compose.overlay.PolylineNode
import com.melody.map.petal_compose.utils.fastFirstOrNull
import java.lang.ref.WeakReference

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

internal class MapApplier(
    val map: HuaweiMap,
    private val mapViewRef: WeakReference<MapView>,
) : AbstractApplier<MapNode>(MapNodeRoot) {

    private val decorations = mutableListOf<MapNode>()

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

    private fun attachClickListeners() {
        // 设置Marker的点击事件，return true拦截
        map.setOnMarkerClickListener { marker ->
            // 优先处理普通Marker的事件，不匹配，再去查找轨迹移动的Marker
            decorations.nodeForMarker(marker)?.onMarkerClick?.invoke(marker)?:false
//            (decorations.nodeForMovingPointOverlay(marker)?.onMarkerClick?.invoke(marker)?: false)
        }
        // Polyline的点击事件
        map.setOnPolylineClickListener {
            decorations.nodeForPolyline(it)?.onPolylineClick?.invoke(it)
//            ?: decorations.nodeForRoutePlanPolyline(it)?.onPolylineClick?.invoke(it)
        }
        // 弹出的InfoWindow的点击事件
        map.setOnInfoWindowClickListener { marker ->
            decorations.nodeForMarker(marker)?.onInfoWindowClick?.invoke(marker)
        }
        // GroundOverlay的点击事件
        map.setOnGroundOverlayClickListener {
            decorations.nodeForGroundOverlay(it)?.onGroundOverlayClick?.invoke(it)
        }
        // Polygon的点击事件
        map.setOnPolygonClickListener {
            decorations.nodeForPolygon(it)?.onPolygonClick?.invoke(it)
        }
        // Circle的点击事件
        map.setOnCircleClickListener {
            decorations.nodeForCircle(it)?.onCircleClick?.invoke(it)
        }
        // 长按触发
        map.setOnMarkerDragListener(object : HuaweiMap.OnMarkerDragListener {
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
        // 设置InfoWindow内容
        map.setInfoWindowAdapter(
            ComposeInfoWindowAdapter(
                mapViewRef,
                markerNodeFinder = {
                    decorations.nodeForMarker(it)
                }
            )
        )
    }
}
/**
 * Marker
 */
private fun MutableList<MapNode>.nodeForMarker(marker: Marker): MarkerNode? =
    fastFirstOrNull { it is MarkerNode && it.marker.title == marker.title && it.marker.snippet == marker.snippet } as? MarkerNode

/**
 * Polyline
 */
private fun MutableList<MapNode>.nodeForPolyline(polyline: Polyline): PolylineNode? =
    fastFirstOrNull { it is PolylineNode && it.polyline == polyline } as? PolylineNode

/**
 * GroundOverlay
 */
private fun MutableList<MapNode>.nodeForGroundOverlay(overlay: GroundOverlay): GroundOverlayNode? =
    fastFirstOrNull { it is GroundOverlayNode && it.groundOverlay == overlay } as? GroundOverlayNode

/**
 * Polygon
 */
private fun MutableList<MapNode>.nodeForPolygon(polygon: Polygon): PolygonNode? =
    fastFirstOrNull { it is PolygonNode && it.polygon == polygon } as? PolygonNode

/**
 * Circle
 */
private fun MutableList<MapNode>.nodeForCircle(circle: Circle): CircleNode? =
    fastFirstOrNull { it is CircleNode && it.circle == circle } as? CircleNode
