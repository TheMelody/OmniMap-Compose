package com.melody.map.compose

import androidx.compose.runtime.*
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.Marker
import com.melody.map.compose.overlay.DragState
import com.melody.map.compose.overlay.MarkerNode

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

internal class MapApplier(
    val map: AMap,
    private val mapView: MapView,
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
        // 设置Marker的点击事件，如果：return true拦截
        map.setOnMarkerClickListener { marker ->
            decorations.nodeForMarker(marker)
                ?.onMarkerClick
                ?.invoke(marker)
                ?: false
        }
        // 弹出的InfoWindow的点击事件
        map.setOnInfoWindowClickListener { marker ->
            decorations.nodeForMarker(marker)
                ?.onInfoWindowClick
                ?.invoke(marker)
        }
        // 长按触发
        map.setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
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
                mapView,
                markerNodeFinder = {
                    decorations.nodeForMarker(it)
                }
            )
        )
    }
}

/**
 * 地图Marker
 */
private fun MutableList<MapNode>.nodeForMarker(marker: Marker): MarkerNode? =
    firstOrNull { it is MarkerNode && it.marker.options.title == marker.options.title && it.marker.options.snippet == marker.options.snippet } as? MarkerNode

