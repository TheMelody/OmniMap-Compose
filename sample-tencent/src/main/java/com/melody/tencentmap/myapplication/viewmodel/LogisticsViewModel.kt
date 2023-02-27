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

package com.melody.tencentmap.myapplication.viewmodel

import com.melody.sample.common.base.BaseViewModel
import com.melody.tencentmap.myapplication.contract.LogisticsContract
import com.melody.tencentmap.myapplication.repo.LogisticsRepository
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.Dispatchers

/**
 * LogisticsViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/23 13:40
 */
class LogisticsViewModel :
    BaseViewModel<LogisticsContract.Event, LogisticsContract.State, LogisticsContract.Effect>() {
    override fun createInitialState(): LogisticsContract.State {
        return LogisticsContract.State(
            isLoading = false,
            fixPolylineRainbow = false,
            fromPoint = LatLng(31.19668, 121.337601),
            toPoint = LatLng(28.194668, 112.976868),
            uiSettings = LogisticsRepository.initMapUiSettings(),
            mapProperties = LogisticsRepository.initMapProperties(),
            routePlanDataState = null
        )
    }

    override fun handleEvents(event: LogisticsContract.Event) {
        when (event) {
            is LogisticsContract.Event.QueryRoutePlan -> {
                asyncLaunch(Dispatchers.IO) {
                    setState { copy(isLoading = true, routePlanDataState = null) }
                    val routePlanResult = kotlin.runCatching {
                        LogisticsRepository.getRoutePlan(
                            fromPoint = currentState.fromPoint,
                            toPoint = currentState.toPoint
                        )
                    }
                    setState { copy(isLoading = false) }
                    if (routePlanResult.isSuccess) {
                        setState { copy(routePlanDataState = routePlanResult.getOrNull()) }
                    } else {
                        setEffect { LogisticsContract.Effect.Toast(routePlanResult.exceptionOrNull()?.message) }
                    }
                }
            }
        }
    }

    fun queryRoutePlan() {
        setEvent(LogisticsContract.Event.QueryRoutePlan)
    }

    fun fixPolylineRainbowBug() {
        setState { copy(fixPolylineRainbow = true) }
    }

}