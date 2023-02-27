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

package com.melody.tencentmap.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.sample.common.utils.showToast
import com.melody.tencentmap.myapplication.contract.LogisticsContract
import com.melody.tencentmap.myapplication.ui.route.LogisticsRouteOverlayContent
import com.melody.tencentmap.myapplication.viewmodel.LogisticsViewModel
import com.melody.ui.components.RedCenterLoading
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * LogisticsScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/23 13:40
 */
@Composable
internal fun LogisticsScreen() {
    val viewModel: LogisticsViewModel = viewModel()
    val cameraPositionState = rememberCameraPositionState()
    val currentState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentState.routePlanDataState) {
        currentState.routePlanDataState?.let {
            // 模仿pdd物流详情页，显示范围
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(it.latLngBounds, 300))
            // 目前腾讯地图的bug，有几率move过程中，首次添加polyline，出现末尾大范围断线的问题
            viewModel.fixPolylineRainbowBug()
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is LogisticsContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TXMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = currentState.mapProperties,
            uiSettings = currentState.uiSettings,
            onMapLoaded = viewModel::queryRoutePlan
        ) {
            if(null != currentState.routePlanDataState && currentState.fixPolylineRainbow) {
                LogisticsRouteOverlayContent(currentState.routePlanDataState!!)
            }
        }
        if(currentState.isLoading) {
            RedCenterLoading()
        }
    }
}