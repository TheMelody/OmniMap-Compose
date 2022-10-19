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

package com.melody.map.myapplication.viewmodel

import com.amap.api.maps.model.LatLng
import com.amap.api.services.route.*
import com.melody.map.myapplication.contract.RoutePlanContract
import com.melody.map.myapplication.model.BusRouteDataState
import com.melody.map.myapplication.model.DrivingRouteDataState
import com.melody.map.myapplication.model.RideRouteDataState
import com.melody.map.myapplication.model.WalkRouteDataState
import com.melody.map.myapplication.repo.RoutePlanRepository
import com.melody.sample.common.base.BaseViewModel

/**
 * RoutePlanViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/14 15:02
 */
class RoutePlanViewModel :
    BaseViewModel<RoutePlanContract.Event, RoutePlanContract.State, RoutePlanContract.Effect>(),
    RouteSearchV2.OnRouteSearchListener {

    private var routeSearch: RouteSearchV2? = null

    override fun createInitialState(): RoutePlanContract.State {
        return RoutePlanContract.State(
            isLoading = false,
            queryStartPoint = LatLng(39.942295, 116.335891),
            queryEndPoint = LatLng(39.995576, 116.481288),
            uiSettings = RoutePlanRepository.initMapUiSettings(),
            mapProperties = RoutePlanRepository.initMapProperties(),
            dataState = null
        )
    }

    override fun handleEvents(event: RoutePlanContract.Event) {
        when(event) {
            is RoutePlanContract.Event.RoadTrafficClick -> {
                setState { copy(mapProperties = mapProperties.copy(isTrafficEnabled = !mapProperties.isTrafficEnabled)) }
            }
            is RoutePlanContract.Event.QueryRoutePlan -> {
                setState { copy(isLoading = true, dataState = null) }
                when (event.queryType) {
                    0 -> queryDrivingRoutePlan()
                    1 -> queryBusRoutePlan()
                    2 -> queryWalkRoutePlan()
                    else -> queryRideRoutePlan()
                }
            }
        }
    }

    init {
        queryRoutePlan(0)
    }

    fun queryRoutePlan(queryType: Int) {
        setEvent(RoutePlanContract.Event.QueryRoutePlan(queryType))
    }

    fun switchRoadTraffic() {
        setEvent(RoutePlanContract.Event.RoadTrafficClick)
    }

    private fun queryDrivingRoutePlan() {
        RoutePlanRepository.drivingRoutePlanSearch(
            startPoint = currentState.queryStartPoint,
            endPoint = currentState.queryEndPoint,
            routeSearch = routeSearch,
            listener = this
        )
    }

    private fun queryBusRoutePlan() {
        RoutePlanRepository.busRoutePlanSearch(
            startPoint = currentState.queryStartPoint,
            endPoint = currentState.queryEndPoint,
            // 北京的城市区号：10
            cityCode = "10",
            routeSearch = routeSearch,
            listener = this
        )
    }

    private fun queryWalkRoutePlan() {
        RoutePlanRepository.walkRoutePlanSearch(
            startPoint = currentState.queryStartPoint,
            endPoint = currentState.queryEndPoint,
            routeSearch = routeSearch,
            listener = this
        )
    }

    private fun queryRideRoutePlan() {
        RoutePlanRepository.rideRoutePlanSearch(
            startPoint = currentState.queryStartPoint,
            endPoint = currentState.queryEndPoint,
            routeSearch = routeSearch,
            listener = this
        )
    }

    override fun onDriveRouteSearched(result: DriveRouteResultV2?, errorCode: Int) {
        setState { copy(isLoading = false) }
        RoutePlanRepository.handleDriveRouteV2Searched(
            result,
            errorCode
        ) { drivePathV2, startPos, endPos, message ->
            if (null != drivePathV2) {
                setState {
                    copy(
                        dataState = DrivingRouteDataState(
                            routeWidth = 30F,
                            startPos = startPos ?: currentState.queryStartPoint,
                            targetPos = endPos ?: currentState.queryEndPoint,
                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
                            drivePathV2List = drivePathV2,
                            driveLineSelectedTexture = RoutePlanRepository.getDrivingCustomTexture(true),
                            driveLineUnSelectedTexture = RoutePlanRepository.getDrivingCustomTexture(false),
                            throughIcon = null,
                            throughPointList = emptyList()
                        )
                    )
                }
            } else {
                setEffect { RoutePlanContract.Effect.Toast(message) }
            }
        }
    }

    override fun onBusRouteSearched(result: BusRouteResultV2?, errorCode: Int) {
        setState { copy(isLoading = false) }
        RoutePlanRepository.handleBusRouteSearched(
            result,
            errorCode
        ) { busPathV2, startPos, endPos, message ->
            if (null != busPathV2) {
                setState {
                    copy(
                        dataState = BusRouteDataState(
                            routeWidth = 30F,
                            startPos = startPos ?: currentState.queryStartPoint,
                            targetPos = endPos ?: currentState.queryEndPoint,
                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
                            busLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
                            busLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
                            busPathV2List = busPathV2
                        )
                    )
                }
            } else {
                setEffect { RoutePlanContract.Effect.Toast(message) }
            }
        }
    }

    override fun onWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int) {
        setState { copy(isLoading = false) }
        RoutePlanRepository.handleWalkRouteSearched(
            result,
            errorCode
        ) { walkPath, startPos, endPos, message ->
            if (null != walkPath) {
                setState {
                    copy(
                        dataState = WalkRouteDataState(
                            routeWidth = 30F,
                            startPos = startPos ?: currentState.queryStartPoint,
                            targetPos = endPos ?: currentState.queryEndPoint,
                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
                            walkLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
                            walkLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
                            walkNodeIcon = null,
                            walkPathList = walkPath
                        )
                    )
                }
            } else {
                setEffect { RoutePlanContract.Effect.Toast(message) }
            }
        }
    }

    override fun onRideRouteSearched(result: RideRouteResultV2?, errorCode: Int) {
        setState { copy(isLoading = false) }
        RoutePlanRepository.handleRideRouteSearched(
            result,
            errorCode
        ) { ridePath, startPos, endPos, message ->
            if (null != ridePath) {
                setState {
                    copy(
                        dataState = RideRouteDataState(
                            routeWidth = 30F,
                            startPos = startPos ?: currentState.queryStartPoint,
                            targetPos = endPos ?: currentState.queryEndPoint,
                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
                            rideLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
                            rideLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
                            rideNodeIcon = null,
                            nodeVisible = false,
                            ridePathList = ridePath
                        )
                    )
                }
            } else {
                setEffect { RoutePlanContract.Effect.Toast(message) }
            }
        }
    }
}