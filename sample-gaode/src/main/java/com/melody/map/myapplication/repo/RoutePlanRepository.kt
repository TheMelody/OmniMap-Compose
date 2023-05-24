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

package com.melody.map.myapplication.repo

import android.graphics.BitmapFactory
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.*
import com.melody.map.gd_compose.model.MapType
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.myapplication.model.BaseRouteDataState
import com.melody.map.myapplication.model.BusRouteDataState
import com.melody.map.myapplication.model.DrivingRouteDataState
import com.melody.map.myapplication.model.RideRouteDataState
import com.melody.map.myapplication.model.WalkRouteDataState
import com.melody.sample.common.utils.SDKUtils
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * RoutePlanRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/14 14:45
 */
object RoutePlanRepository {

    fun initMapUiSettings() : MapUiSettings {
        return MapUiSettings(
            isZoomEnabled = true,
            isScrollGesturesEnabled = true,
            isZoomGesturesEnabled = true,
            isScaleControlsEnabled = true
        )
    }

    fun initMapProperties() : MapProperties {
        return MapProperties(mapType = MapType.NAVI, isTrafficEnabled = false)
    }

    fun getStartMarkerIcon(): BitmapDescriptor {
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
    }

    fun getDrivingCustomTexture(isSelected: Boolean): BitmapDescriptor? {
        val result = kotlin.runCatching {
            val assetsStream = SDKUtils.getApplicationContext().assets.open(if(isSelected) {
                "ic_map_route_status_default_selected.png"
            } else{
                "ic_map_route_status_default.png"
            })
            val textureBitmap = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeStream(assetsStream))
            assetsStream.close()
            textureBitmap
        }
        return result.getOrNull()
    }

    fun getBusCustomTexture(isSelected: Boolean): BitmapDescriptor? {
        val result = kotlin.runCatching {
            val assetsStream = SDKUtils.getApplicationContext().assets.open(if(isSelected) {
                "ic_map_route_status_green_selected.png"
            } else{
                "ic_map_route_status_green.png"
            })
            val textureBitmap = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeStream(assetsStream))
            assetsStream.close()
            textureBitmap
        }
        return result.getOrNull()
    }

    suspend fun getRoutePlanResult(queryType: Int,startPoint: LatLng, endPoint: LatLng, cityCode: String): BaseRouteDataState {
        return when (queryType) {
            0 -> drivingRoutePlanSearch(startPoint,endPoint)
            1 -> busRoutePlanSearch(startPoint,endPoint,cityCode)
            2 -> walkRoutePlanSearch(startPoint,endPoint)
            else -> rideRoutePlanSearch(startPoint,endPoint)
        }
    }

    /**
     * 驾车路径规划搜索
     */
    private suspend fun drivingRoutePlanSearch(startPoint: LatLng, endPoint: LatLng): BaseRouteDataState {
        return suspendCancellableCoroutine { coroutine ->
            val newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(object : RouteSearchV2.OnRouteSearchListener {
                override fun onDriveRouteSearched(result: DriveRouteResultV2?, errorCode: Int) {
                    if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                        if (result?.paths != null) {
                            if (result.paths.isNotEmpty()) {
                                coroutine.resumeWith(
                                    Result.success(
                                        DrivingRouteDataState(
                                            routeWidth = 30F,
                                            startPos = LatLng(result.startPos.latitude, result.startPos.longitude),
                                            targetPos =LatLng(result.targetPos.latitude, result.targetPos.longitude),
                                            startMarkerIcon = getStartMarkerIcon(),
                                            endMarkerIcon = getEndMarkerIcon(),
                                            startGuideIcon = getStartGuideIcon(),
                                            endGuideIcon = getEndGuideIcon(),
                                            drivePathV2List = result.paths,
                                            driveLineSelectedTexture = getDrivingCustomTexture(true),
                                            driveLineUnSelectedTexture = getDrivingCustomTexture(false),
                                            throughIcon = null,
                                            throughPointList = emptyList()
                                        )
                                    )
                                )
                                return
                            }
                        }
                        coroutine.resumeWith(Result.failure(NullPointerException("没有搜索到相关数据")))
                    } else {
                        coroutine.resumeWith(Result.failure(NullPointerException(errorCode.toString())))
                    }
                }
                override fun onBusRouteSearched(p0: BusRouteResultV2?, p1: Int) {
                }
                override fun onWalkRouteSearched(p0: WalkRouteResultV2?, p1: Int) {
                }
                override fun onRideRouteSearched(p0: RideRouteResultV2?, p1: Int) {
                }
            })
            val fromAndTo = RouteSearchV2.FromAndTo(
                LatLonPoint(startPoint.latitude, startPoint.longitude),
                LatLonPoint(endPoint.latitude, endPoint.longitude)
            )
            // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            val query = RouteSearchV2.DriveRouteQuery(
                fromAndTo,
                RouteSearchV2.DrivingStrategy.DEFAULT,
                null,
                null,
                ""
            )
            // 一定要设置，否则如下的字段都没有数据
            query.showFields =
                RouteSearchV2.ShowFields.POLINE or RouteSearchV2.ShowFields.CITIES or RouteSearchV2.ShowFields.COST or RouteSearchV2.ShowFields.NAVI or RouteSearchV2.ShowFields.TMCS
            // 异步路径规划驾车模式查询
            newRouteSearch.calculateDriveRouteAsyn(query)
        }
    }

    /**
     * 公交车路径规划搜索:
     * http://a.amap.com/lbs/static/unzip/Android_Map_Doc/Search/com/amap/api/services/route/RouteSearchV2.BusRouteQuery.html
     */
    private suspend fun busRoutePlanSearch(startPoint: LatLng, endPoint: LatLng, cityCode: String): BaseRouteDataState {
        return suspendCancellableCoroutine { coroutine ->
            val newRouteSearch  = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(object : RouteSearchV2.OnRouteSearchListener {
                override fun onBusRouteSearched(result: BusRouteResultV2?, errorCode: Int) {
                    if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
                        if (result?.paths != null) {
                            if (result.paths.isNotEmpty()) {
                                coroutine.resumeWith(Result.success(
                                    BusRouteDataState(
                                    routeWidth = 30F,
                                    startPos = LatLng(result.startPos.latitude, result.startPos.longitude),
                                    targetPos =LatLng(result.targetPos.latitude, result.targetPos.longitude),
                                    startMarkerIcon = getStartMarkerIcon(),
                                    endMarkerIcon = getEndMarkerIcon(),
                                    startGuideIcon = getStartGuideIcon(),
                                    endGuideIcon = getEndGuideIcon(),
                                    busLineSelectedTexture = getBusCustomTexture(true),
                                    busLineUnSelectedTexture = getBusCustomTexture(false),
                                    busPathV2List = result.paths
                                )))
                                return
                            }
                        }
                        coroutine.resumeWith(Result.failure(NullPointerException("没有搜索到相关数据")))
                    } else {
                        coroutine.resumeWith(Result.failure(NullPointerException(errorCode.toString())))
                    }
                }
                override fun onDriveRouteSearched(p0: DriveRouteResultV2?, p1: Int) {
                }
                override fun onWalkRouteSearched(p0: WalkRouteResultV2?, p1: Int) {
                }
                override fun onRideRouteSearched(p0: RideRouteResultV2?, p1: Int) {
                }
            })
            val fromAndTo = RouteSearchV2.FromAndTo(
                LatLonPoint(startPoint.latitude, startPoint.longitude),
                LatLonPoint(endPoint.latitude, endPoint.longitude)
            )
            // 第一个参数：路径的起终点。
            // 第二个参数：计算路径的模式。可选，默认为最快捷 RouteSearchV2.BusMode。
            // 第三个参数：城市区号/电话区号。此项不能为空。
            // 第四个参数：是否计算夜班车，默认为不计算。0：不计算，1：计算。可选。
            val query = RouteSearchV2.BusRouteQuery(fromAndTo, RouteSearchV2.BusMode.BUS_DEFAULT, cityCode, 1)
            query.showFields = RouteSearchV2.ShowFields.ALL
            newRouteSearch.calculateBusRouteAsyn(query)
        }
    }

    /**
     * 步行路径规划搜索
     */
    private suspend fun walkRoutePlanSearch(startPoint: LatLng,endPoint: LatLng): BaseRouteDataState {
        return suspendCancellableCoroutine { coroutine ->
            val newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(object : RouteSearchV2.OnRouteSearchListener {
                override fun onWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int) {
                    if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
                        if (result?.paths != null) {
                            if (result.paths.isNotEmpty()) {
                                coroutine.resumeWith(Result.success(
                                    WalkRouteDataState(
                                        routeWidth = 30F,
                                        startPos = LatLng(result.startPos.latitude, result.startPos.longitude),
                                        targetPos = LatLng(result.targetPos.latitude, result.targetPos.longitude),
                                        startMarkerIcon = getStartMarkerIcon(),
                                        endMarkerIcon = getEndMarkerIcon(),
                                        startGuideIcon = getStartGuideIcon(),
                                        endGuideIcon = getEndGuideIcon(),
                                        walkLineSelectedTexture = getBusCustomTexture(true),
                                        walkLineUnSelectedTexture = getBusCustomTexture(false),
                                        walkNodeIcon = null,
                                        walkPathList = result.paths
                                    )
                                ))
                                return
                            }
                        }
                        coroutine.resumeWith(Result.failure(NullPointerException("没有搜索到相关数据")))
                    } else {
                        coroutine.resumeWith(Result.failure(NullPointerException(errorCode.toString())))
                    }
                }
                override fun onDriveRouteSearched(p0: DriveRouteResultV2?, p1: Int) {
                }
                override fun onBusRouteSearched(p0: BusRouteResultV2?, p1: Int) {
                }
                override fun onRideRouteSearched(p0: RideRouteResultV2?, p1: Int) {
                }
            })
            val fromAndTo = RouteSearchV2.FromAndTo(
                LatLonPoint(startPoint.latitude, startPoint.longitude),
                LatLonPoint(endPoint.latitude, endPoint.longitude)
            )
            val query = RouteSearchV2.WalkRouteQuery(fromAndTo)
            query.showFields = RouteSearchV2.ShowFields.ALL
            newRouteSearch.calculateWalkRouteAsyn(query)
        }
    }

    /**
     * 骑行路径规划搜索
     */
    private suspend fun rideRoutePlanSearch(startPoint: LatLng, endPoint: LatLng): BaseRouteDataState {
        return suspendCancellableCoroutine { coroutine ->
            val newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(object : RouteSearchV2.OnRouteSearchListener {
                override fun onRideRouteSearched(result: RideRouteResultV2?, errorCode: Int) {
                    if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
                        if (result?.paths != null) {
                            if (result.paths.isNotEmpty()) {
                                coroutine.resumeWith(Result.success(
                                    RideRouteDataState(
                                        routeWidth = 30F,
                                        startPos = LatLng(result.startPos.latitude, result.startPos.longitude),
                                        targetPos = LatLng(result.targetPos.latitude, result.targetPos.longitude),
                                        startMarkerIcon = getStartMarkerIcon(),
                                        endMarkerIcon = getEndMarkerIcon(),
                                        startGuideIcon = getStartGuideIcon(),
                                        endGuideIcon = getEndGuideIcon(),
                                        rideLineSelectedTexture = getBusCustomTexture(true),
                                        rideLineUnSelectedTexture = getBusCustomTexture(false),
                                        rideNodeIcon = null,
                                        nodeVisible = false,
                                        ridePathList = result.paths
                                    )
                                ))
                                return
                            }
                        }
                        coroutine.resumeWith(Result.failure(NullPointerException("没有搜索到相关数据")))
                    } else {
                        coroutine.resumeWith(Result.failure(NullPointerException(errorCode.toString())))
                    }
                }
                override fun onDriveRouteSearched(p0: DriveRouteResultV2?, p1: Int) {
                }
                override fun onBusRouteSearched(p0: BusRouteResultV2?, p1: Int) {
                }
                override fun onWalkRouteSearched(p0: WalkRouteResultV2?, p1: Int) {
                }
            })
            val fromAndTo = RouteSearchV2.FromAndTo(
                LatLonPoint(startPoint.latitude, startPoint.longitude),
                LatLonPoint(endPoint.latitude, endPoint.longitude)
            )
            val query = RouteSearchV2.RideRouteQuery(fromAndTo)
            query.showFields = RouteSearchV2.ShowFields.ALL
            newRouteSearch.calculateRideRouteAsyn(query)
        }
    }
}