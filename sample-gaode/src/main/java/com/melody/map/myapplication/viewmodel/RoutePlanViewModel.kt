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
import com.melody.map.myapplication.contract.RoutePlanContract
import com.melody.map.myapplication.repo.RoutePlanRepository
import com.melody.sample.common.base.BaseViewModel
import kotlinx.coroutines.Dispatchers

/**
 * RoutePlanViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/14 15:02
 */
class RoutePlanViewModel :
    BaseViewModel<RoutePlanContract.Event, RoutePlanContract.State, RoutePlanContract.Effect>(){

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
                asyncLaunch(Dispatchers.IO) {
                    val result = kotlin.runCatching {
                        RoutePlanRepository.getRoutePlanResult(
                            queryType = event.queryType,
                            startPoint = currentState.queryStartPoint,
                            endPoint = currentState.queryEndPoint,
                            // 北京的城市区号：10
                            cityCode = "10"
                        )
                    }
                    if(result.isSuccess) {
                        setState { copy(isLoading = false, dataState = result.getOrNull()) }
                    } else {
                        setState { copy(isLoading = false) }
                        setEffect { RoutePlanContract.Effect.Toast(result.exceptionOrNull()?.message) }
                    }
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
}