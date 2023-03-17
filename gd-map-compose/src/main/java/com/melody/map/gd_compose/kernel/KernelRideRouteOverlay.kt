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
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.amap.api.services.route.RidePath
import com.amap.api.services.route.RideStep

/**
 * 核心取自【高德路径规划】示例代码里面的RideRouteOverlay，骑行线路路径规划
 */
internal class KernelRideRouteOverlay(
    aMap: AMap,
    isSelected: Boolean,
    routeWidth: Float,
    polylineColor: Color,
    private val rideLineSelectedTexture: BitmapDescriptor?,
    private val rideLineUnSelectedTexture: BitmapDescriptor?,
    private val rideStationDescriptor: BitmapDescriptor?,
    startPoint: LatLng,
    endPoint: LatLng,
    private val ridePath: RidePath?
) : KernelRouteOverlay(
    aMap = aMap,
    isSelected = isSelected,
    routeWidth = routeWidth,
    polylineColor = polylineColor,
    busLineSelectedTexture = null,
    busLineUnSelectedTexture = null,
    walkLineSelectedTexture = null,
    walkLineUnSelectedTexture = null,
    driveLineSelectedTexture = null,
    driveLineUnSelectedTexture = null,
    busNodeIcon = null,
    walkNodeIcon = null,
    driveNodeIcon = null,
    startPoint = startPoint,
    endPoint = endPoint
) {
    private var mPolylineOptions: PolylineOptions? = null

    /**
     * 添加骑行路线到地图中
     */
    fun addToMap() {
        if (routeWidth == 0f || ridePath == null) return
        asyncLaunch {
            removeFromMap()
            initPolylineOptions()
            val ridePaths = ridePath.steps
            mPolylineOptions?.add(startPoint)
            for (i in ridePaths.indices) {
                val rideStep = ridePaths[i]
                convertToLatLng(rideStep?.polyline?.getOrNull(0))?.let {
                    addRideStationMarkers(
                        rideStep,
                        it
                    )
                }
                addRidePolyLines(rideStep)
            }
            mPolylineOptions?.add(endPoint)
            showPolyline()
            if(isSelected) {
                zoomToSpan()
            }
        }
    }

    private fun addRidePolyLines(rideStep: RideStep?) {
        mPolylineOptions?.addAll(convertArrList(rideStep?.polyline ?: emptyList()))
    }

    private fun addRideStationMarkers(rideStep: RideStep, position: LatLng) {
        addStationMarker(
            MarkerOptions()
                .position(position)
                .title(
                    "\u65B9\u5411:" + rideStep.action
                            + "\n\u9053\u8DEF:" + rideStep.road
                )
                .snippet(rideStep.instruction).visible(nodeIconVisible)
                .anchor(0.5f, 0.5f).icon(rideStationDescriptor)
        )
    }

    private fun initPolylineOptions() {
        mPolylineOptions = null
        mPolylineOptions = PolylineOptions()
        setPolylineSelected(isSelected)
    }

    override fun setPolylineSelected(isSelected: Boolean) {
        mPolylineOptions
            ?.color(polylineColor.copy(alpha = if (isSelected) ROUTE_SELECTED_TRANSPARENCY else ROUTE_UNSELECTED_TRANSPARENCY).toArgb())
            ?.zIndex(if (isSelected) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
            ?.setCustomTexture(if (isSelected) rideLineSelectedTexture else rideLineUnSelectedTexture)
            ?.width(routeWidth)
        removeAllPolyLines()
        showPolyline()
    }

    private fun showPolyline() {
        addPolyLine(mPolylineOptions)
    }
}