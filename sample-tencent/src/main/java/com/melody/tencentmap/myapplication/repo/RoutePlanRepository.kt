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

import com.melody.map.tencent_compose.model.MapType
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.tencentmap.myapplication.model.BaseRouteDataState
import com.melody.tencentmap.myapplication.model.BusRouteDataState
import com.melody.tencentmap.myapplication.model.DrivingRouteDataState
import com.melody.tencentmap.myapplication.model.RideRouteDataState
import com.melody.tencentmap.myapplication.model.WalkRouteDataState
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.HttpResponseListener
import com.tencent.lbssearch.`object`.param.BicyclingParam
import com.tencent.lbssearch.`object`.param.DrivingParam
import com.tencent.lbssearch.`object`.param.TransitParam
import com.tencent.lbssearch.`object`.param.WalkingParam
import com.tencent.lbssearch.`object`.result.BicyclingResultObject
import com.tencent.lbssearch.`object`.result.DrivingResultObject
import com.tencent.lbssearch.`object`.result.TransitResultObject
import com.tencent.lbssearch.`object`.result.WalkingResultObject
import com.tencent.tencentmap.mapsdk.maps.model.Animation
import com.tencent.tencentmap.mapsdk.maps.model.AnimationListener
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
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true,
            isScaleControlsEnabled = true
        )
    }

    fun initMapProperties() : MapProperties {
        return MapProperties(mapType = MapType.NORMAL, isTrafficEnabled = false)
    }

    /**
     * 路径规划的线段动画
     */
    private fun initPolylineAnimation(
        startLatLng: LatLng,
        totalDuration: Int
    ): Animation {
        return EmergeAnimation(startLatLng).apply {
            duration = totalDuration.toLong()
        }
    }

    private fun convertLatLngBounds(allPolyLines: List<LatLng>): LatLngBounds {
        val b: LatLngBounds.Builder = LatLngBounds.builder()
        for (point in allPolyLines) {
            b.include(point)
        }
        return b.build()
    }

    /**
     * 查询路径规划
     */
    suspend fun queryRoutePlan(queryType: Int, fromPoint:LatLng, toPoint:LatLng): BaseRouteDataState {
        return when(queryType) {
            0 -> drivingRoutePlanSearch(fromPoint, toPoint)
            1 -> busRoutePlanSearch(fromPoint, toPoint)
            2 -> walkRoutePlanSearch(fromPoint, toPoint)
            else -> rideRoutePlanSearch(fromPoint, toPoint)
        }
    }

     /**
     * 驾车路径规划搜索
     */
    private suspend fun drivingRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): DrivingRouteDataState {
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
                    // 返回多路径
                    val points= (p1.result.routes?.map { it.polyline }?: emptyList())

                    continuation.resumeWith(Result.success(
                        DrivingRouteDataState(
                            polylineWidth = 24F,
                            polylineBorderWidth = 6F,
                            startPoint = fromPoint,
                            endPoint = toPoint,
                            latLngBounds = convertLatLngBounds(points[0]),
                            polylineAnim = initPolylineAnimation(fromPoint,1000),
                            points = points,
                            isAnimationStart = false,
                            isAnimationEnd = false
                        )
                    ))
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
    private suspend fun busRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): BusRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val transitParam = TransitParam(fromPoint, toPoint)
            val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            transitParam.policy(TransitParam.Policy.LEAST_WALKING, TransitParam.Preference.NO_SUBWAY)
            tencentSearch.getRoutePlan(transitParam,object : HttpResponseListener<TransitResultObject?> {
                override fun onSuccess(p0: Int, p1: TransitResultObject?) {
                    if(p1?.result == null) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    if (p1.result != null && p1.result.routes != null && p1.result.routes.size > 0) {
                        continuation.resumeWith(Result.success(BusRouteDataState(
                            polylineWidth = 24F,
                            polylineBorderWidth = 6F,
                            startPoint = fromPoint,
                            endPoint = toPoint,
                            latLngBounds = LatLngBounds(p1.result.routes[0].bounds.northeast,p1.result.routes[0].bounds.southwest),
                            routeList = p1.result.routes
                        )))
                    } else {
                        continuation.resumeWith(Result.failure(NullPointerException("路线结果为空")))
                    }
                }
                override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(p1)))
                }
            })
        }
    }

    /**
     * 步行路径规划搜索
     */
    private suspend fun walkRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): WalkRouteDataState{
        return suspendCancellableCoroutine { continuation ->
            val walkingParam = WalkingParam()
            walkingParam.from(fromPoint)
            walkingParam.to(toPoint)
            val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            tencentSearch.getRoutePlan(walkingParam, object : HttpResponseListener<WalkingResultObject?> {
                override fun onSuccess(p0: Int, p1: WalkingResultObject?) {
                    if (p1 == null) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    if (p1.result != null && p1.result.routes != null && p1.result.routes.size > 0) {
                        val walkPointList = p1.result.routes.map { it.polyline }
                        continuation.resumeWith(Result.success(
                            WalkRouteDataState(
                                startPoint = fromPoint,
                                endPoint = toPoint,
                                polylineWidth = 10F,
                                latLngBounds = convertLatLngBounds(walkPointList[0]),
                                wakingPoints = walkPointList
                            )
                        ))
                    } else {
                        continuation.resumeWith(Result.failure(NullPointerException("路线结果为空")))
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    continuation.resumeWith(Result.failure(Throwable(responseString?:throwable?.message)))
                }
            })
        }
    }

    /**
     * 骑行路径规划搜索
     */
    private suspend fun rideRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): RideRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val bicyclingParam = BicyclingParam()
            bicyclingParam.from(fromPoint)
            bicyclingParam.to(toPoint)
            val tencentSearch = TencentSearch(SDKUtils.getApplicationContext(),WEB_SERVICE_API_SECRET_KEY)
            tencentSearch.getRoutePlan(bicyclingParam, object : HttpResponseListener<BicyclingResultObject>{
                override fun onSuccess(p0: Int, p1: BicyclingResultObject?) {
                    if (p1 == null) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    if (p1.result != null && p1.result.routes != null && p1.result.routes.size > 0) {
                        val ridePointList = p1.result.routes.map { it.polyline }
                        continuation.resumeWith(Result.success(
                            RideRouteDataState(
                                startPoint = fromPoint,
                                endPoint = toPoint,
                                polylineWidth = 24F,
                                polylineBorderWidth = 6F,
                                polylineAnim = initPolylineAnimation(fromPoint, 500),
                                latLngBounds = convertLatLngBounds(ridePointList[0]),
                                ridePoints = ridePointList
                            )
                        ))
                    } else {
                        continuation.resumeWith(Result.failure(NullPointerException("路线结果为空")))
                    }
                }

                override fun onFailure(p0: Int, responseString: String?, p2: Throwable?) {
                    continuation.resumeWith(Result.failure(Throwable(responseString)))
                }
            })
        }
    }
}