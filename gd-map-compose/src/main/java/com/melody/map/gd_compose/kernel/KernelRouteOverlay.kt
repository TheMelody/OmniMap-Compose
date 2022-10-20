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

import androidx.compose.ui.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 核心取自【高德路径规划】示例代码里面的RouteOverlay
 */
internal open class KernelRouteOverlay(
    val aMap: AMap,
    val isSelected: Boolean,
    val routeWidth: Float,
    val polylineColor: Color,
    val walkLineSelectedTexture: BitmapDescriptor?,
    val walkLineUnSelectedTexture: BitmapDescriptor?,
    val busLineSelectedTexture: BitmapDescriptor?,
    val busLineUnSelectedTexture: BitmapDescriptor?,
    val driveLineSelectedTexture: BitmapDescriptor?,
    val driveLineUnSelectedTexture: BitmapDescriptor?,
    val busNodeIcon: BitmapDescriptor?,
    val walkNodeIcon: BitmapDescriptor?,
    val driveNodeIcon: BitmapDescriptor?,
    var startPoint: LatLng,
    var endPoint: LatLng
) {
    companion object {
        const val ROUTE_UNSELECTED_TRANSPARENCY = 0.3F
        const val ROUTE_SELECTED_TRANSPARENCY = 1F
        const val ROUTE_SELECTED_ZINDEX = 0F
        const val ROUTE_UNSELECTED_ZINDEX = -1F
    }

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    var stationMarkers: MutableList<Marker> = mutableListOf()
    var allPolyLines: MutableList<Polyline> = mutableListOf()
    var nodeIconVisible: Boolean = true

    fun convertToLatLng(latLonPoint: LatLonPoint?): LatLng? {
        if(null == latLonPoint) return null
        return LatLng(latLonPoint.latitude, latLonPoint.longitude)
    }

    open fun setPolylineSelected(isSelected: Boolean){
    }

    fun convertArrList(shapes: List<LatLonPoint>): ArrayList<LatLng> {
        val lineShapes: ArrayList<LatLng> = ArrayList()
        for (point in shapes) {
           convertToLatLng(point)?.let { lineShapes.add(it) }
        }
        return lineShapes
    }

    /**
     * 去掉BusRouteOverlay上所有的Marker。
     */
    open fun removeFromMap() {
        removeAllMarkers()
        removeAllPolyLines()
    }

    private fun removeAllMarkers() {
        for (marker in stationMarkers) {
            marker.remove()
        }
    }

    fun removeAllPolyLines() {
        for (line in allPolyLines) {
            line.remove()
        }
    }

    open fun getLatLngBounds(): LatLngBounds {
        val b: LatLngBounds.Builder = LatLngBounds.builder()
        b.include(LatLng(startPoint.latitude, startPoint.longitude))
        b.include(LatLng(endPoint.latitude, endPoint.longitude))
        for (polyline in allPolyLines) {
            for (point in polyline.points) {
                b.include(point)
            }
        }
        return b.build()
    }

    /**
     * 移动镜头到当前的视角。
     */
    fun zoomToSpan() {
        kotlin.runCatching {
            val bounds: LatLngBounds = getLatLngBounds()
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    /**
     * 路段节点图标控制显示接口。
     * @param visible true为显示节点图标，false为不显示。
     */
    fun setNodeIconVisibility(visible: Boolean) {
        kotlin.runCatching {
            nodeIconVisible = visible
            if (stationMarkers.size > 0) {
                for (i in stationMarkers.indices) {
                    if(null != stationMarkers[i].icons[0].bitmap) {
                        stationMarkers[i].isVisible = visible
                    }
                }
            }
        }
    }

    fun addStationMarker(options: MarkerOptions?) {
        if (options == null) {
            return
        }
        aMap.addMarker(options)?.let { stationMarkers.add(it) }
    }

    fun addPolyLine(options: PolylineOptions?) {
        if (options == null) {
            return
        }
        aMap.addPolyline(options)?.let {  allPolyLines.add(it) }
    }
}