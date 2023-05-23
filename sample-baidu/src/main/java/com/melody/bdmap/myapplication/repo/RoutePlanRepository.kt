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

package com.melody.bdmap.myapplication.repo

import com.baidu.mapapi.map.OverlayOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.route.BikingRoutePlanOption
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRoutePlanOption
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRoutePlanOption
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRoutePlanOption
import com.baidu.mapapi.search.route.WalkingRouteResult
import com.melody.bdmap.myapplication.model.BaseRouteDataState
import com.melody.bdmap.myapplication.model.BusRouteDataState
import com.melody.bdmap.myapplication.model.DrivingRouteDataState
import com.melody.bdmap.myapplication.model.IRouteSearchResultListener
import com.melody.bdmap.myapplication.model.RideRouteDataState
import com.melody.bdmap.myapplication.model.WalkRouteDataState
import com.melody.bdmap.myapplication.utils.BDRoutePlanUtils
import com.melody.map.baidu_compose.model.MapType
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * RoutePlanRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/05/09 16:21
 */
object RoutePlanRepository {

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
     * 查询路径规划
     */
    suspend fun queryRoutePlan(queryType: Int, fromPoint:LatLng, toPoint:LatLng): BaseRouteDataState? {
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
            val search = RoutePlanSearch.newInstance()
            search.setOnGetRoutePlanResultListener(object :IRouteSearchResultListener{
                override fun onGetDrivingRouteResult(result: DrivingRouteResult?) {
                    if (result != null && result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                        // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                        // result.getSuggestAddrInfo()
                        continuation.resumeWith(Result.failure(Exception("起终点或途经点地址有岐义,通过 result.getSuggestAddrInfo()接口获取建议查询信息")))
                        return
                    }
                    if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                        continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        return
                    }
                    if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                        if (result.routeLines.size > 1) {
                            val routeList = mutableListOf<List<OverlayOptions>?>()
                            result.routeLines.forEach {
                                routeList.add(BDRoutePlanUtils.getDrivingOverlayOptions(it))
                            }
                            continuation.resumeWith(Result.success(
                                DrivingRouteDataState(
                                    startPoint = fromPoint,
                                    endPoint = toPoint,
                                    listOverlayOptions = routeList,
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    selectTextureList = BDRoutePlanUtils.getDrivingRouteSelectTextureList(),
                                    unSelectTextureList = BDRoutePlanUtils.getDrivingRouteUnSelectTextureList()
                                )
                            ))
                        } else if (result.routeLines.size == 1) {
                            val routeLine = result.routeLines.getOrNull(0)
                            val list = BDRoutePlanUtils.getDrivingOverlayOptions(routeLine)
                            continuation.resumeWith(Result.success(
                                DrivingRouteDataState(
                                    startPoint = routeLine?.starting?.location ?: fromPoint,
                                    endPoint = routeLine?.terminal?.location ?: toPoint,
                                    listOverlayOptions = listOf(list),
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    selectTextureList = BDRoutePlanUtils.getDrivingRouteSelectTextureList(),
                                    unSelectTextureList = BDRoutePlanUtils.getDrivingRouteUnSelectTextureList()
                                )
                            ))
                        } else {
                            continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                            return
                        }
                    }
                }
            })
            val drivingRoutePlanOption = DrivingRoutePlanOption()
            // 时间优先策略，默认时间优先
            drivingRoutePlanOption.policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST)
            // 开启路况，可以获取拥堵情况
            drivingRoutePlanOption.trafficPolicy(DrivingRoutePlanOption.DrivingTrafficPolicy.ROUTE_PATH_AND_TRAFFIC)
            val startNode = PlanNode.withLocation(fromPoint)
            val endNode = PlanNode.withLocation(toPoint)
            search.drivingSearch(drivingRoutePlanOption.from(startNode).to(endNode))
        }
    }

    /**
     * 公交车路径规划搜索
     */
    private suspend fun busRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): BusRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val search = RoutePlanSearch.newInstance()
            search.setOnGetRoutePlanResultListener(object :IRouteSearchResultListener{
                override fun onGetTransitRouteResult(result: TransitRouteResult?) {
                    if (result != null && result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                        // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                        // result.getSuggestAddrInfo()
                        continuation.resumeWith(Result.failure(Exception("起终点或途经点地址有岐义,通过 result.getSuggestAddrInfo()接口获取建议查询信息")))
                        return
                    }
                    if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                        continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        return
                    }
                    if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                        if (result.routeLines.size > 1) {
                            val routeList = mutableListOf<List<OverlayOptions>?>()
                            result.routeLines.forEach {
                                routeList.add(BDRoutePlanUtils.getBusOverlayOptions(it))
                            }
                            continuation.resumeWith(Result.success(
                                BusRouteDataState(
                                    startPoint = fromPoint,
                                    endPoint = toPoint,
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    listOverlayOptions = routeList,
                                    selectTexture = BDRoutePlanUtils.getOtherRouteSelectTexture(),
                                    unSelectTexture = BDRoutePlanUtils.getOtherRouteUnSelectTexture()
                                )
                            ))
                        } else if (result.routeLines.size == 1) {
                            val routeLine = result.routeLines.getOrNull(0)
                            val list = BDRoutePlanUtils.getBusOverlayOptions(routeLine)
                            continuation.resumeWith(Result.success(
                                BusRouteDataState(
                                    startPoint = fromPoint,
                                    endPoint = toPoint,
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    listOverlayOptions = listOf(list),
                                    selectTexture = BDRoutePlanUtils.getOtherRouteSelectTexture(),
                                    unSelectTexture = BDRoutePlanUtils.getOtherRouteUnSelectTexture()
                                )
                            ))
                        } else {
                            continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        }
                    }
                }
            })
            val startNode = PlanNode.withLocation(fromPoint)
            val endNode = PlanNode.withLocation(toPoint)
            search.transitSearch((TransitRoutePlanOption()) // 使用默认的策略
                .from(startNode)
                .to(endNode).city("成都")) // 您如果copy，copy后，请您自行指定endNode位置的城市
        }
    }

    /**
     * 步行路径规划搜索
     */
    private suspend fun walkRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): WalkRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val search = RoutePlanSearch.newInstance()
            search.setOnGetRoutePlanResultListener(object :IRouteSearchResultListener{
                override fun onGetWalkingRouteResult(result: WalkingRouteResult?) {
                    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        return
                    }
                    if ( result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                        // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                        // result.getSuggestAddrInfo()
                        continuation.resumeWith(Result.failure(Exception("起终点或途经点地址有岐义,通过 result.getSuggestAddrInfo()接口获取建议查询信息")))
                        return
                    }
                    if (result.routeLines.size > 1) {
                        val routeList = mutableListOf<List<OverlayOptions>?>()
                        result.routeLines.forEach {
                            routeList.add(BDRoutePlanUtils.getWalkOverlayOptions(it))
                        }
                        continuation.resumeWith(Result.success(
                            WalkRouteDataState(
                                startPoint = fromPoint,
                                endPoint = toPoint,
                                latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                listOverlayOptions = routeList,
                                selectTexture = BDRoutePlanUtils.getOtherRouteSelectTexture(),
                                unSelectTexture = BDRoutePlanUtils.getOtherRouteUnSelectTexture()
                            )
                        ))
                    } else if (result.routeLines.size == 1) {
                        val routeLine = result.routeLines.getOrNull(0)
                        val list = BDRoutePlanUtils.getWalkOverlayOptions(routeLine)
                        continuation.resumeWith(Result.success(
                            WalkRouteDataState(
                                startPoint = fromPoint,
                                endPoint = toPoint,
                                latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                listOverlayOptions = listOf(list),
                                selectTexture = null,
                                unSelectTexture = null
                            )
                        ))
                    } else {
                        continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                    }
                }
            })
            val startNode = PlanNode.withLocation(fromPoint)
            val endNode = PlanNode.withLocation(toPoint)
            search.walkingSearch((WalkingRoutePlanOption())
                .from(startNode) // 起点
                .to(endNode)) // 终点
        }
    }

    /**
     * 骑行路径规划搜索
     */
    private suspend fun rideRoutePlanSearch(fromPoint:LatLng, toPoint:LatLng): RideRouteDataState {
        return suspendCancellableCoroutine { continuation ->
            val search = RoutePlanSearch.newInstance()
            search.setOnGetRoutePlanResultListener(object :IRouteSearchResultListener{
                override fun onGetBikingRouteResult(result: BikingRouteResult?) {
                    if(null == result || result.error != SearchResult.ERRORNO.NO_ERROR) {
                        continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        return
                    }
                    if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                        // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                        // result.getSuggestAddrInfo()
                        continuation.resumeWith(Result.failure(Exception("起终点或途经点地址有岐义,通过 result.getSuggestAddrInfo()接口获取建议查询信息")))
                        return
                    }
                    if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                        if (result.routeLines.size > 1) {
                            val routeList = mutableListOf<List<OverlayOptions>?>()
                            result.routeLines.forEach {
                                routeList.add(BDRoutePlanUtils.getRideOverlayOptions(it))
                            }
                            continuation.resumeWith(Result.success(
                                RideRouteDataState(
                                    startPoint = fromPoint,
                                    endPoint = toPoint,
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    listOverlayOptions = routeList,
                                    selectTexture = BDRoutePlanUtils.getOtherRouteSelectTexture(),
                                    unSelectTexture = BDRoutePlanUtils.getOtherRouteUnSelectTexture()
                                )
                            ))
                        } else if (result.routeLines.size == 1) {
                            val routeLine = result.routeLines.getOrNull(0)
                            val list = BDRoutePlanUtils.getRideOverlayOptions(routeLine)
                            continuation.resumeWith(Result.success(
                                RideRouteDataState(
                                    startPoint = fromPoint,
                                    endPoint = toPoint,
                                    latLngBounds = BDRoutePlanUtils.getOverlayLatLngBounds(fromPoint,toPoint),
                                    listOverlayOptions = listOf(list),
                                    selectTexture = BDRoutePlanUtils.getOtherRouteSelectTexture(),
                                    unSelectTexture = BDRoutePlanUtils.getOtherRouteUnSelectTexture()
                                )
                            ))
                        }  else {
                            continuation.resumeWith(Result.failure(Exception("抱歉，未找到结果")))
                        }
                    }
                }
            })
            val startNode = PlanNode.withLocation(fromPoint)
            val endNode = PlanNode.withLocation(toPoint)
            // ridingType这里设置成0，表示：用普通模式骑行，设置成1就是电动车模式
            val bikingRoutePlanOption = BikingRoutePlanOption().from(startNode).to(endNode).ridingType(0)
            // 发起骑行路线规划检索
            search.bikingSearch(bikingRoutePlanOption)
        }
    }
}