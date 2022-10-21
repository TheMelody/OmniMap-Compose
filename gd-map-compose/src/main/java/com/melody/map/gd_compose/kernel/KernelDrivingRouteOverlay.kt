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

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.DrivePathV2
import com.amap.api.services.route.DriveStepV2
import com.amap.api.services.route.TMC
import com.melody.map.gd_compose.R
import kotlinx.coroutines.*

/**
 * 核心取自【高德路径规划】示例代码里面的DrivingRouteOverlay，驾车线路路径规划
 */
internal class KernelDrivingRouteOverlay(
    aMap: AMap,
    isSelected: Boolean,
    routeWidth: Float,
    polylineColor: Color,
    driveLineSelectedTexture: BitmapDescriptor,
    driveLineUnSelectedTexture: BitmapDescriptor,
    driveNodeIcon: BitmapDescriptor?,
    private val throughMarkerIcon: BitmapDescriptor?,
    startPoint: LatLng,
    endPoint: LatLng,
    private val drivePath: DrivePathV2?,
    private val throughPointList: List<LatLonPoint>
) : KernelRouteOverlay(
    aMap = aMap,
    isSelected = isSelected,
    routeWidth = routeWidth,
    polylineColor = polylineColor,
    walkLineSelectedTexture = null,
    walkLineUnSelectedTexture = null,
    busLineSelectedTexture = null,
    busLineUnSelectedTexture = null,
    driveLineSelectedTexture = driveLineSelectedTexture,
    driveLineUnSelectedTexture = driveLineUnSelectedTexture,
    busNodeIcon = null,
    walkNodeIcon = null,
    driveNodeIcon = driveNodeIcon,
    startPoint = startPoint,
    endPoint = endPoint
) {
    companion object {
        private const val TAG = "DrivingRouteOverlay"
    }
    private val throughPointMarkerList: MutableList<Marker> = mutableListOf()
    private var mLatLngsOfPath: MutableList<LatLng>? = null
    private var tmcs: MutableList<TMC>? = null
    private var mPolylineOptions: PolylineOptions? = null
    private var mPolylineOptionscolor: PolylineOptions? = null
    private var throughPointMarkerVisible = true
    private var isColorfulline = true
    private var isAddToMapFinish: Boolean = false

    /**
     * 初始化线段属性
     */
    private fun initPolylineOptions() {
        mPolylineOptions = null
        mPolylineOptions = PolylineOptions()
        setPolylineTexture(isSelected)
    }

    private suspend fun resolveLatLngPoints(stepList: List<DriveStepV2>):List<LatLng> {
        return suspendCancellableCoroutine { continuation ->
            mLatLngsOfPath = mutableListOf()
            val points:MutableList<LatLng> = mutableListOf()
            for (step in stepList) {
                step.tmCs?.let { tmcs?.addAll(it) }
                step.polyline?.forEachIndexed { index, latLonPoint ->
                    convertToLatLng(latLonPoint)?.let {
                        if(index == 0) {
                            addDrivingStationMarkers(step, it)
                        }
                        points.add(it)
                        mLatLngsOfPath?.add(it)
                    }
                }
            }
            continuation.resumeWith(Result.success(points))
        }
    }

    private suspend fun resolveColorLineTextureList(tmcSection: List<TMC>,isSelected:Boolean) : Map<Int,BitmapDescriptor>{
        return suspendCancellableCoroutine { continuation ->
            var segmentTrafficStatus: TMC
            var textureIndex = 0
            val customTextureMap: MutableMap<Int, BitmapDescriptor> = mutableMapOf()
            mPolylineOptionscolor?.zIndex(if(isSelected) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
            mPolylineOptionscolor?.add(startPoint)
            mPolylineOptionscolor?.add(
                convertToLatLng(tmcSection.getOrNull(0)?.polyline?.getOrNull(0))
            )
            if(isSelected) {
                driveLineSelectedTexture?.let { customTextureMap.put(textureIndex, it) }
            } else {
                driveLineUnSelectedTexture?.let { customTextureMap.put(textureIndex, it)  }
            }
            textureIndex++
            for (i in tmcSection.indices) {
                segmentTrafficStatus = tmcSection[i]
                val colorResID = getStatusColorDrawable(segmentTrafficStatus.status,isSelected)
                val ployLines = segmentTrafficStatus.polyline
                for (j in 1 until ployLines.size) {
                    mPolylineOptionscolor?.add(convertToLatLng(ployLines.getOrNull(j)))
                    customTextureMap[textureIndex] = BitmapDescriptorFactory.fromResource(colorResID)
                    textureIndex++
                }
            }
            mPolylineOptionscolor?.add(endPoint)
            if(isSelected) {
                driveLineSelectedTexture?.let { customTextureMap.put(textureIndex, it) }
            } else {
                driveLineUnSelectedTexture?.let { customTextureMap.put(textureIndex, it)  }
            }
            continuation.resumeWith(Result.success(customTextureMap))
        }
    }

    /**
     * 添加驾车路线添加到地图上显示。
     */
    fun addToMap() {
        if (routeWidth == 0f || drivePath == null) return
        asyncLaunch {
            removeFromMap()
            initPolylineOptions()
            val result = kotlin.runCatching {
                tmcs = mutableListOf()
                mPolylineOptions?.add(startPoint)
                resolveLatLngPoints(drivePath.steps).forEach {
                    mPolylineOptions?.add(it)
                }
                mPolylineOptions?.add(endPoint)
                addThroughPointMarker()
                setPolylineSelected(isSelected)
            }
            isAddToMapFinish = true
            if (isSelected) {
                zoomToSpan()
            }
            if(result.isFailure) {
                Log.e(TAG,"addToMap",result.exceptionOrNull())
            }
        }
    }

    /**
     * 是否显示颜色拥堵情况
     */
    fun setIsColorfulline(iscolorfulline: Boolean) {
        isColorfulline = iscolorfulline
    }

    override fun setPolylineSelected(isSelected: Boolean) {
        if(!isAddToMapFinish) return
        asyncLaunch {
            val drawColorTexture = isColorfulline && tmcs?.isNotEmpty() == true
            if(drawColorTexture) {
                colorWayUpdate(tmcs, isSelected)
            } else {
                setPolylineTexture(isSelected)
            }
            removeAllPolyLines()
            if(drawColorTexture) {
                showColorPolyline()
            } else {
                showPolyline()
            }
        }
    }

    private fun setPolylineTexture(isSelected: Boolean) {
        mPolylineOptions?.setCustomTexture(if (isSelected) driveLineSelectedTexture else driveLineUnSelectedTexture)
            ?.width(routeWidth)
            ?.zIndex(if(isSelected) ROUTE_SELECTED_ZINDEX else ROUTE_UNSELECTED_ZINDEX)
            ?.color(
                polylineColor.copy(alpha = if (isSelected) ROUTE_SELECTED_TRANSPARENCY else ROUTE_UNSELECTED_TRANSPARENCY)
                    .toArgb()
            )?.setDottedLine(true)?.dottedLineType = 1
    }

    private fun showColorPolyline() {
        addPolyLine(mPolylineOptionscolor)
    }

    private fun showPolyline() {
        addPolyLine(mPolylineOptions)
    }

    private fun addDrivingStationMarkers(driveStep: DriveStepV2, latLng: LatLng) {
        addStationMarker(
            MarkerOptions()
                .position(latLng)
                .title("\u65B9\u5411:${driveStep.navi.action}\n\u9053\u8DEF:${driveStep.road}")
                .snippet(driveStep.instruction)
                .visible(null != driveNodeIcon && nodeIconVisible)
                .anchor(0.5f, 0.5f).icon(driveNodeIcon)
        )
    }

    /**
     * 添加途经点Marker
     */
    private fun addThroughPointMarker() {
        if (throughPointList.isNotEmpty()) {
            var latLonPoint: LatLonPoint?
            for (i in throughPointList.indices) {
                latLonPoint = throughPointList[i]
                throughPointMarkerList.add(
                    aMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(latLonPoint.latitude, latLonPoint.longitude))
                            .visible(throughPointMarkerVisible)
                            .icon(throughMarkerIcon)
                            .title("\u9014\u7ECF\u70B9")
                    )
                )
            }
        }
    }

    /**
     * 根据不同的路段拥堵情况展示不同的颜色
     *
     * @param tmcSection
     */
    private suspend fun colorWayUpdate(tmcSection: List<TMC>?, isSelected: Boolean) {
        if (tmcSection == null || tmcSection.isEmpty()) {
            return
        }
        mPolylineOptionscolor = null
        mPolylineOptionscolor = PolylineOptions()
        mPolylineOptionscolor?.isUseTexture = true
        mPolylineOptionscolor?.width(routeWidth)
        val customTextureMap = resolveColorLineTextureList(tmcSection,isSelected)
        // 图片List和Index的List都要设置，否则无法正常加载出全部状态图片
        mPolylineOptionscolor?.customTextureIndex = customTextureMap.keys.toList()
        mPolylineOptionscolor?.customTextureList = customTextureMap.values.toList()
    }

    private fun getStatusColorDrawable(status: String,isSelected: Boolean): Int {
        return when (status) {
            "畅通" -> {
                if(isSelected) {
                    R.drawable.ic_map_route_status_green_selected
                } else {
                    R.drawable.ic_map_route_status_green
                }
            }
            "缓行" -> {
                if(isSelected) {
                    R.drawable.ic_map_route_status_yellow_selected
                } else {
                    R.drawable.ic_map_route_status_yellow
                }
            }
            "拥堵" -> {
                if(isSelected) {
                    R.drawable.ic_map_route_status_red_selected
                } else {
                    R.drawable.ic_map_route_status_red
                }
            }
            "严重拥堵" -> {
                if(isSelected) {
                    R.drawable.ic_map_route_status_deepred_selected
                } else {
                    R.drawable.ic_map_route_status_red
                }
            }
            else -> {
                if(isSelected) {
                    R.drawable.ic_map_route_status_default_selected
                } else {
                    R.drawable.ic_map_route_status_default
                }
            }
        }
    }

    fun setThroughPointIconVisibility(visible: Boolean) {
        val result = kotlin.runCatching {
            throughPointMarkerVisible = visible
            if (throughPointMarkerList.size > 0) {
                for (i in throughPointMarkerList.indices) {
                    throughPointMarkerList[i].isVisible = visible
                }
            }
        }
        if(result.isFailure) {
            Log.e(TAG,"setThroughPointIconVisibility", result.exceptionOrNull())
        }
    }

    override fun getLatLngBounds(): LatLngBounds {
        val b = LatLngBounds.builder()
        b.include(LatLng(startPoint.latitude, startPoint.longitude))
        b.include(LatLng(endPoint.latitude, endPoint.longitude))
        if (throughPointList.isNotEmpty()) {
            for (i in throughPointList.indices) {
                b.include(
                    LatLng(
                        throughPointList[i].latitude,
                        throughPointList[i].longitude
                    )
                )
            }
        }
        return b.build()
    }

    override fun removeFromMap() {
        val result = kotlin.runCatching {
            super.removeFromMap()
            if (throughPointMarkerList.size > 0) {
                for (i in throughPointMarkerList.indices) {
                    throughPointMarkerList[i].remove()
                }
                throughPointMarkerList.clear()
            }
        }
        isAddToMapFinish = false
        if(result.isFailure) {
            Log.e(TAG,"removeFromMap",result.exceptionOrNull())
        }
    }
}