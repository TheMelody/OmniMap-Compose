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

package com.melody.tencentmap.myapplication.repo

import android.graphics.BitmapFactory
import com.melody.map.tencent_compose.model.MapType
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.HttpResponseListener
import com.tencent.lbssearch.`object`.param.DrivingParam
import com.tencent.lbssearch.`object`.param.TransitParam
import com.tencent.lbssearch.`object`.result.DrivingResultObject
import com.tencent.lbssearch.`object`.result.TransitResultObject
import com.tencent.tencentmap.mapsdk.maps.model.Animation
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.EmergeAnimation
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * RoutePlanRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/17 16:45
 */
object RoutePlanRepository {

    /**
     * 勾选WebService API，点击签名校验，复制代码的话，【你要自己替换成你自己的SECRET_KEY】
     */
    private const val WEB_SERVICE_API_SECRET_KEY = "W79RgYY0lOIrzukvPoLM2E0DZjkKg4Cj"

    fun initMapUiSettings() : MapUiSettings {
        return MapUiSettings(
            isZoomEnabled = true,
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true,
            isScaleControlsEnabled = true
        )
    }

    fun initMapProperties() : MapProperties {
        return MapProperties(mapType = MapType.NORMAL, isTrafficEnabled = false)
    }

    /*fun getStartMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.bus_start_icon
            )
        )
    }

    fun getEndMarkerIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.bus_end_icon
            )
        )
    }

    fun getStartGuideIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.ic_map_start_guide_icon
            )
        )
    }

    fun getEndGuideIcon(): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                com.melody.ui.components.R.drawable.ic_map_end_guide_icon
            )
        )
    }*/

    fun getDrivingCustomTexture(isSelected: Boolean): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                if(isSelected) {
                    com.melody.ui.components.R.drawable.ic_map_route_status_default_selected
                } else{
                    com.melody.ui.components.R.drawable.ic_map_route_status_default
                }
            )
        )
    }

    fun getBusCustomTexture(isSelected: Boolean): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(
            BitmapFactory.decodeResource(
                SDKUtils.getApplicationContext().resources,
                if(isSelected) {
                    com.melody.ui.components.R.drawable.ic_map_route_status_green_selected
                } else{
                    com.melody.ui.components.R.drawable.ic_map_route_status_green
                }
            )
        )
    }

    /**
     * 路径规划的线段动画
     */
    fun initPolylineAnimation(startLatLng: LatLng, totalDuration: Int): Animation {
        return EmergeAnimation(startLatLng).apply {
            duration = totalDuration.toLong()
        }
    }

    fun convertLatLngBounds(allPolyLines: List<LatLng>): LatLngBounds {
        val b: LatLngBounds.Builder = LatLngBounds.builder()
        for (point in allPolyLines) {
            b.include(point)
        }
        return b.build()
    }

     /**
     * 驾车路径规划搜索
     */
    suspend fun drivingRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): List<List<LatLng>> {
        return suspendCancellableCoroutine { continuation ->
            val drivingParam = DrivingParam(fromPoint, toPoint) //创建导航参数
            drivingParam.roadType(DrivingParam.RoadType.ON_MAIN_ROAD_BELOW_BRIDGE)
            drivingParam.heading(90)
            drivingParam.accuracy(30)
            val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            tencentSearch.getRoutePlan(drivingParam, object : HttpResponseListener<DrivingResultObject> {
                override fun onSuccess(p0: Int, p1 : DrivingResultObject?) {
                    if(p1?.result == null) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    continuation.resumeWith(Result.success((p1.result.routes?.map { it.polyline }?: emptyList())))
                }

                override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(p1)))
                }
            })
        }
    }

    /**
     * 公交车路径规划搜索
     */
    fun busRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng) {
        val transitParam = TransitParam(fromPoint, toPoint)
        val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
        transitParam.policy(TransitParam.Policy.LEAST_WALKING, TransitParam.Preference.NO_SUBWAY)
        tencentSearch.getRoutePlan(transitParam,object : HttpResponseListener<TransitResultObject?> {
                override fun onSuccess(p0: Int, p1: TransitResultObject?) {

                }
                override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                }
        })
    }

    /**
     * 步行路径规划搜索
     */
    fun walkRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng) {

    }

    /**
     * 骑行路径规划搜索
     */
    fun rideRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng) {

    }
}