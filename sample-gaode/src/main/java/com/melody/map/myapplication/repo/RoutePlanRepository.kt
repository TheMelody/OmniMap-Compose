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
import com.melody.sample.common.utils.SDKUtils

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
            showMapLogo = true,
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
     * 驾车路径规划搜索
     */
    fun drivingRoutePlanSearch(
        startPoint: LatLng,
        endPoint: LatLng,
        routeSearch: RouteSearchV2?,
        listener: RouteSearchV2.OnRouteSearchListener
    ) {
        val newRouteSearch: RouteSearchV2
        if (null == routeSearch) {
            newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(listener)
        } else {
            newRouteSearch = routeSearch
        }
        val fromAndTo = RouteSearchV2.FromAndTo(LatLonPoint(startPoint.latitude,startPoint.longitude), LatLonPoint(endPoint.latitude,endPoint.longitude))
        // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
        val query = RouteSearchV2.DriveRouteQuery(
            fromAndTo,
            RouteSearchV2.DrivingStrategy.DEFAULT,
            null,
            null,
            ""
        )
        // 一定要设置，否则如下的字段都没有数据
        query.showFields = RouteSearchV2.ShowFields.POLINE or RouteSearchV2.ShowFields.CITIES or RouteSearchV2.ShowFields.COST or RouteSearchV2.ShowFields.NAVI or RouteSearchV2.ShowFields.TMCS
        // 异步路径规划驾车模式查询
        newRouteSearch.calculateDriveRouteAsyn(query)
    }

    /**
     * 公交车路径规划搜索:
     * http://a.amap.com/lbs/static/unzip/Android_Map_Doc/Search/com/amap/api/services/route/RouteSearchV2.BusRouteQuery.html
     */
    fun busRoutePlanSearch(
        startPoint: LatLng,
        endPoint: LatLng,
        cityCode: String,
        routeSearch: RouteSearchV2?,
        listener: RouteSearchV2.OnRouteSearchListener
    ) {
        val newRouteSearch: RouteSearchV2
        if (null == routeSearch) {
            newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(listener)
        } else {
            newRouteSearch = routeSearch
        }
        val fromAndTo = RouteSearchV2.FromAndTo(LatLonPoint(startPoint.latitude,startPoint.longitude), LatLonPoint(endPoint.latitude,endPoint.longitude))
        // 第一个参数：路径的起终点。
        // 第二个参数：计算路径的模式。可选，默认为最快捷 RouteSearchV2.BusMode。
        // 第三个参数：城市区号/电话区号。此项不能为空。
        // 第四个参数：是否计算夜班车，默认为不计算。0：不计算，1：计算。可选。
        val query = RouteSearchV2.BusRouteQuery(fromAndTo, RouteSearchV2.BusMode.BUS_LEASE_WALK, cityCode, 1)
        query.showFields = RouteSearchV2.ShowFields.ALL
        newRouteSearch.calculateBusRouteAsyn(query)
    }

    /**
     * 步行路径规划搜索
     */
    fun walkRoutePlanSearch(
        startPoint: LatLng,
        endPoint: LatLng,
        routeSearch: RouteSearchV2?,
        listener: RouteSearchV2.OnRouteSearchListener
    ) {
        val newRouteSearch: RouteSearchV2
        if (null == routeSearch) {
            newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(listener)
        } else {
            newRouteSearch = routeSearch
        }
        val fromAndTo = RouteSearchV2.FromAndTo(LatLonPoint(startPoint.latitude,startPoint.longitude), LatLonPoint(endPoint.latitude,endPoint.longitude))
        val query = RouteSearchV2.WalkRouteQuery(fromAndTo)
        query.showFields = RouteSearchV2.ShowFields.ALL
        newRouteSearch.calculateWalkRouteAsyn(query)
    }

    /**
     * 骑行路径规划搜索
     */
    fun rideRoutePlanSearch(
        startPoint: LatLng,
        endPoint: LatLng,
        routeSearch: RouteSearchV2?,
        listener: RouteSearchV2.OnRouteSearchListener
    ) {
        val newRouteSearch: RouteSearchV2
        if (null == routeSearch) {
            newRouteSearch = RouteSearchV2(SDKUtils.getApplicationContext())
            newRouteSearch.setRouteSearchListener(listener)
        } else {
            newRouteSearch = routeSearch
        }
        val fromAndTo = RouteSearchV2.FromAndTo(LatLonPoint(startPoint.latitude,startPoint.longitude), LatLonPoint(endPoint.latitude,endPoint.longitude))
        val query = RouteSearchV2.RideRouteQuery(fromAndTo)
        query.showFields = RouteSearchV2.ShowFields.ALL
        newRouteSearch.calculateRideRouteAsyn(query)
    }

    /**
     * 处理驾车路径V2接口搜索结果
     */
    inline fun handleDriveRouteV2Searched(
        result: DriveRouteResultV2?,
        errorCode: Int,
        block: (List<DrivePathV2>?, LatLng?, LatLng?, String?) -> Unit
    ) {
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.paths != null) {
                if (result.paths.isNotEmpty()) {
                    val startPos = LatLng(result.startPos.latitude, result.startPos.longitude)
                    val endPos = LatLng(result.targetPos.latitude, result.targetPos.longitude)
                    block.invoke(result.paths, startPos, endPos, null)
                    return
                }
            }
            block.invoke(null, null, null, "没有搜索到相关数据")
        } else {
            block.invoke(null, null, null, errorCode.toString())
        }
    }

    inline fun handleBusRouteSearched(result: BusRouteResultV2?, errorCode: Int, block: (List<BusPathV2>?, LatLng?, LatLng?, String?) -> Unit) {
        if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.paths != null) {
                if (result.paths.isNotEmpty()) {
                    val startPos = LatLng(result.startPos.latitude, result.startPos.longitude)
                    val endPos = LatLng(result.targetPos.latitude, result.targetPos.longitude)
                    block.invoke(result.paths,startPos, endPos, null)
                    return
                }
            }
            block.invoke(null, null, null, "没有搜索到相关数据")
        } else {
            block.invoke(null, null, null, errorCode.toString())
        }
    }

    inline fun handleWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int, block: (List<WalkPath>?, LatLng?, LatLng?, String?) -> Unit) {
        if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.paths != null) {
                if (result.paths.isNotEmpty()) {
                    val busPath = result.paths[0]
                    val startPos = LatLng(result.startPos.latitude, result.startPos.longitude)
                    val endPos = LatLng(result.targetPos.latitude, result.targetPos.longitude)
                    block.invoke(result.paths,startPos, endPos, null)
                    return
                }
            }
            block.invoke(null, null, null, "没有搜索到相关数据")
        } else {
            block.invoke(null, null, null, errorCode.toString())
        }
    }

    inline fun handleRideRouteSearched(result: RideRouteResultV2?, errorCode: Int, block: (List<RidePath>?, LatLng?, LatLng?, String?) -> Unit) {
        if(errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result?.paths != null) {
                if (result.paths.isNotEmpty()) {
                    val startPos = LatLng(result.startPos.latitude, result.startPos.longitude)
                    val endPos = LatLng(result.targetPos.latitude, result.targetPos.longitude)
                    block.invoke(result.paths,startPos, endPos, null)
                    return
                }
            }
            block.invoke(null, null, null, "没有搜索到相关数据")
        } else {
            block.invoke(null, null, null, errorCode.toString())
        }
    }
}