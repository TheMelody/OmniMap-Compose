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

package com.melody.tencentmap.myapplication.contract

import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState
import com.melody.tencentmap.myapplication.model.BaseRouteDataState
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

/**
 * RoutePlanContract
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/14 14:45
 */
class RoutePlanContract {

    sealed class Event : IUiEvent {
        object RoadTrafficClick: Event()
        data class QueryRoutePlan(val queryType: Int): Event()
    }

    data class State (
        val isLoading: Boolean,
        val fromPoint: LatLng,
        val toPoint: LatLng,
        val uiSettings: MapUiSettings,
        val mapProperties: MapProperties,
        val routePlanDataState: BaseRouteDataState?
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal class Toast(val msg: String?): Effect()
    }
}

