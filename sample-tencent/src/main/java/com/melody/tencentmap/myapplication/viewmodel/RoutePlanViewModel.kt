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

package com.melody.tencentmap.myapplication.viewmodel

import com.melody.sample.common.base.BaseViewModel
import com.melody.tencentmap.myapplication.contract.RoutePlanContract
import com.melody.tencentmap.myapplication.model.DrivingRouteDataState
import com.melody.tencentmap.myapplication.repo.RoutePlanRepository
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.Dispatchers

/**
 * RoutePlanViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/17 16:42
 */
class RoutePlanViewModel :
    BaseViewModel<RoutePlanContract.Event, RoutePlanContract.State, RoutePlanContract.Effect>(){

    override fun createInitialState(): RoutePlanContract.State {
        return RoutePlanContract.State(
            isLoading = false,
            fromPoint = LatLng(24.66493, 117.09568),
            toPoint = LatLng(26.8857, 120.00514),
            uiSettings = RoutePlanRepository.initMapUiSettings(),
            mapProperties = RoutePlanRepository.initMapProperties(),
            routePlanDataState = null
        )
    }

    override fun handleEvents(event: RoutePlanContract.Event) {
        when(event) {
            is RoutePlanContract.Event.RoadTrafficClick -> {
                setState { copy(mapProperties = mapProperties.copy(isTrafficEnabled = !mapProperties.isTrafficEnabled)) }
            }
            is RoutePlanContract.Event.QueryRoutePlan -> {
                setState { copy(isLoading = true/*, dataState = null*/) }
                when (event.queryType) {
                    0 -> queryDrivingRoutePlan()
                    1 -> queryBusRoutePlan()
                    2 -> queryWalkRoutePlan()
                    else -> queryRideRoutePlan()
                }
            }
        }
    }

    fun queryRoutePlan(queryType: Int = 0) {
        setEvent(RoutePlanContract.Event.QueryRoutePlan(queryType))
    }

    fun switchRoadTraffic() {
        setEvent(RoutePlanContract.Event.RoadTrafficClick)
    }

    private fun queryDrivingRoutePlan() = asyncLaunch(Dispatchers.IO){
        val drivingRoutePlanResult = kotlin.runCatching {
            RoutePlanRepository.drivingRoutePlanSearch(
                fromPoint = currentState.fromPoint,
                toPoint = currentState.toPoint
            )
        }
        if(drivingRoutePlanResult.isSuccess) {
            val points = drivingRoutePlanResult.getOrNull() ?: emptyList()
            setState {
                copy(
                    routePlanDataState = DrivingRouteDataState(
                        polylineWidth = 24F,
                        polylineBorderWidth = 6F,
                        startPoint = currentState.fromPoint,
                        endPoint = currentState.toPoint,
                        latLngBounds = RoutePlanRepository.convertLatLngBounds(points[0]),
                        polylineAnim = RoutePlanRepository.initPolylineAnimation(currentState.fromPoint,2000),
                        points = points
                    )
                )
            }
        }else{
            setEffect { RoutePlanContract.Effect.Toast(drivingRoutePlanResult.exceptionOrNull()?.message) }
        }
        setState { copy(isLoading = false) }
    }

    private fun queryBusRoutePlan() = asyncLaunch(Dispatchers.IO){
        RoutePlanRepository.busRoutePlanSearch(
            fromPoint = currentState.fromPoint,
            toPoint = currentState.toPoint
        )
    }

    private fun queryWalkRoutePlan() = asyncLaunch(Dispatchers.IO){
        RoutePlanRepository.walkRoutePlanSearch(
            fromPoint = currentState.fromPoint,
            toPoint = currentState.toPoint
        )
    }

    private fun queryRideRoutePlan() = asyncLaunch(Dispatchers.IO){
        RoutePlanRepository.rideRoutePlanSearch(
            fromPoint = currentState.fromPoint,
            toPoint = currentState.toPoint
        )
    }

//    override fun onDriveRouteSearched(result: DriveRouteResultV2?, errorCode: Int) {
//        setState { copy(isLoading = false) }
//        RoutePlanRepository.handleDriveRouteV2Searched(
//            result,
//            errorCode
//        ) { drivePathV2, startPos, endPos, message ->
//            if (null != drivePathV2) {
//                setState {
//                    copy(
//                        dataState = DrivingRouteDataState(
//                            routeWidth = 30F,
//                            startPos = startPos ?: currentState.fromPoint,
//                            targetPos = endPos ?: currentState.toPoint,
//                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
//                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
//                            startGuideIcon = RoutePlanRepository.getStartGuideIcon(),
//                            endGuideIcon = RoutePlanRepository.getEndGuideIcon(),
//                            drivePathV2List = drivePathV2,
//                            driveLineSelectedTexture = RoutePlanRepository.getDrivingCustomTexture(true),
//                            driveLineUnSelectedTexture = RoutePlanRepository.getDrivingCustomTexture(false),
//                            throughIcon = null,
//                            throughPointList = emptyList()
//                        )
//                    )
//                }
//            } else {
//                setEffect { RoutePlanContract.Effect.Toast(message) }
//            }
//        }
//    }
//
//    override fun onBusRouteSearched(result: BusRouteResultV2?, errorCode: Int) {
//        setState { copy(isLoading = false) }
//        RoutePlanRepository.handleBusRouteSearched(
//            result,
//            errorCode
//        ) { busPathV2, startPos, endPos, message ->
//            if (null != busPathV2) {
//                setState {
//                    copy(
//                        dataState = BusRouteDataState(
//                            routeWidth = 30F,
//                            startPos = startPos ?: currentState.fromPoint,
//                            targetPos = endPos ?: currentState.toPoint,
//                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
//                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
//                            startGuideIcon = RoutePlanRepository.getStartGuideIcon(),
//                            endGuideIcon = RoutePlanRepository.getEndGuideIcon(),
//                            busLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
//                            busLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
//                            busPathV2List = busPathV2
//                        )
//                    )
//                }
//            } else {
//                setEffect { RoutePlanContract.Effect.Toast(message) }
//            }
//        }
//    }
//
//    override fun onWalkRouteSearched(result: WalkRouteResultV2?, errorCode: Int) {
//        setState { copy(isLoading = false) }
//        RoutePlanRepository.handleWalkRouteSearched(
//            result,
//            errorCode
//        ) { walkPath, startPos, endPos, message ->
//            if (null != walkPath) {
//                setState {
//                    copy(
//                        dataState = WalkRouteDataState(
//                            routeWidth = 30F,
//                            startPos = startPos ?: currentState.fromPoint,
//                            targetPos = endPos ?: currentState.toPoint,
//                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
//                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
//                            startGuideIcon = RoutePlanRepository.getStartGuideIcon(),
//                            endGuideIcon = RoutePlanRepository.getEndGuideIcon(),
//                            walkLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
//                            walkLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
//                            walkNodeIcon = null,
//                            walkPathList = walkPath
//                        )
//                    )
//                }
//            } else {
//                setEffect { RoutePlanContract.Effect.Toast(message) }
//            }
//        }
//    }
//
//    override fun onRideRouteSearched(result: RideRouteResultV2?, errorCode: Int) {
//        setState { copy(isLoading = false) }
//        RoutePlanRepository.handleRideRouteSearched(
//            result,
//            errorCode
//        ) { ridePath, startPos, endPos, message ->
//            if (null != ridePath) {
//                setState {
//                    copy(
//                        dataState = RideRouteDataState(
//                            routeWidth = 30F,
//                            startPos = startPos ?: currentState.fromPoint,
//                            targetPos = endPos ?: currentState.toPoint,
//                            startMarkerIcon = RoutePlanRepository.getStartMarkerIcon(),
//                            endMarkerIcon = RoutePlanRepository.getEndMarkerIcon(),
//                            startGuideIcon = RoutePlanRepository.getStartGuideIcon(),
//                            endGuideIcon = RoutePlanRepository.getEndGuideIcon(),
//                            rideLineSelectedTexture = RoutePlanRepository.getBusCustomTexture(true),
//                            rideLineUnSelectedTexture = RoutePlanRepository.getBusCustomTexture(false),
//                            rideNodeIcon = null,
//                            nodeVisible = false,
//                            ridePathList = ridePath
//                        )
//                    )
//                }
//            } else {
//                setEffect { RoutePlanContract.Effect.Toast(message) }
//            }
//        }
//    }
}