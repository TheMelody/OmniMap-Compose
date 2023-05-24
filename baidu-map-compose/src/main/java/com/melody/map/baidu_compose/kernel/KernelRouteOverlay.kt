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

package com.melody.map.baidu_compose.kernel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLngBounds
import com.melody.map.baidu_compose.model.RoutePlanType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * KernelRouteOverlay 核心取自百度地图Demo里面的OverlayManager
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/05/09 16:44
 */
internal class KernelRouteOverlay(
    private val baiduMap:BaiduMap,
    private var isSelected: Boolean,
    private val routeWidth: Int,
    private val latLngBounds: LatLngBounds,
    private val routePlanType: RoutePlanType,
    private val polylineColor: Color?,
    private val selectTextureList: List<BitmapDescriptor?>?,
    private val unSelectTextureList: List<BitmapDescriptor?>?,
    private val overlayOptions: List<OverlayOptions>
) {
    private val mOverlayOptions: ArrayList<OverlayOptions> = arrayListOf()
    private val mOverlayList: MutableList<Overlay> = mutableListOf()
    private val asyncJobs: MutableList<Job> = mutableListOf()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun getAllOverlayList(): List<Overlay> = mOverlayList

    private fun asyncLaunch(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) = coroutineScope.launch(context = context) {
        block.invoke(this)
    }.apply {
        asyncJobs.add(this)
    }

    fun setPolylineSelected(isSelected: Boolean) {
        this.isSelected = isSelected
        synchronized(baiduMap) {
            addToMap(false)
        }
    }

    /**
     * 将所有Overlay 添加到地图上
     */
    fun addToMap(zoomToSpan: Boolean = true) {
        asyncLaunch {
            removeFromMap(false)
            mOverlayOptions.addAll(overlayOptions)
            for (option in mOverlayOptions) {
                if(option is PolylineOptions) {
                    mOverlayList.add(baiduMap.addOverlay(
                        option.width(routeWidth).zIndex(if(isSelected) 1 else 0).apply {
                            if(polylineColor != null) {
                                color(polylineColor.copy(alpha = if(isSelected) 1F else 0.4F).toArgb())
                            }
                            if(routePlanType == RoutePlanType.DRIVING && selectTextureList?.isNotEmpty() == true) {
                                customTextureList(if(isSelected) selectTextureList else unSelectTextureList)
                            } else {
                                var renderTexture = true
                                if(routePlanType == RoutePlanType.BUS_LINE) {
                                    renderTexture = !extraInfo.getBoolean("render_point",false)
                                }
                                if(renderTexture) {
                                    if(isSelected){
                                        selectTextureList?.getOrNull(0)?.let { customTexture(it) }
                                    } else {
                                        unSelectTextureList?.getOrNull(0)?.let { customTexture(it) }
                                    }
                                }
                            }
                        }
                    ))
                }
            }
            if(zoomToSpan) {
                zoomToSpan()
            }
        }
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    fun removeFromMap(isClear: Boolean = true) {
        for (overlay in mOverlayList) {
            overlay.remove()
        }
        mOverlayList.clear()
        mOverlayOptions.clear()
        if(isClear) {
            val iterator = asyncJobs.iterator()
            while(iterator.hasNext()) {
                val job = iterator.next()
                if(!job.isCancelled) {
                    job.cancel()
                }
                iterator.remove()
            }
        }
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     */
    private fun zoomToSpan() {
        if (mOverlayList.size > 0) {
            val mapStatus: MapStatus? = baiduMap.mapStatus
            if (null != mapStatus) {
                val width: Int =
                    mapStatus.winRound.right - baiduMap.mapStatus.winRound.left - 400
                val height: Int =
                    mapStatus.winRound.bottom - baiduMap.mapStatus.winRound.top - 400
                baiduMap.animateMapStatus(MapStatusUpdateFactory
                    .newLatLngBounds(latLngBounds, width, height))

            }
        }
    }

    /**
     * 设置显示在规定宽高中的地图地理范围
     */
    private fun zoomToSpanPaddingBounds(
        paddingLeft: Int,
        paddingTop: Int,
        paddingRight: Int,
        paddingBottom: Int
    ) {
        if (mOverlayList.size > 0) {
            baiduMap.animateMapStatus(
                MapStatusUpdateFactory
                    .newLatLngBounds(
                        latLngBounds,
                        paddingLeft,
                        paddingTop,
                        paddingRight,
                        paddingBottom
                    )
            )
        }
    }
}