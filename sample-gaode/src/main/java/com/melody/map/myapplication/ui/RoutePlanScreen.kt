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

package com.melody.map.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.position.rememberCameraPositionState
import com.melody.map.myapplication.contract.RoutePlanContract
import com.melody.map.myapplication.model.BusRouteDataState
import com.melody.map.myapplication.model.DrivingRouteDataState
import com.melody.map.myapplication.model.RideRouteDataState
import com.melody.map.myapplication.model.WalkRouteDataState
import com.melody.map.myapplication.ui.route.BusRouteOverlayContent
import com.melody.map.myapplication.ui.route.DrivingRouteOverlayContent
import com.melody.map.myapplication.ui.route.RideRouteOverlayContent
import com.melody.map.myapplication.ui.route.WalkRouteOverlayContent
import com.melody.map.myapplication.viewmodel.RoutePlanViewModel
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.MapMenuButton
import com.melody.ui.components.ReadCenterLoading
import com.melody.ui.components.RoadTrafficSwitch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * RoutePlanScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/13 11:58
 */
@Composable
internal fun RoutePlanScreen() {
    val cameraPositionState = rememberCameraPositionState()
    val viewModel: RoutePlanViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is RoutePlanContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = currentState.mapProperties,
            uiSettings = currentState.uiSettings,
            onMapLoaded = viewModel::queryRoutePlan
        ) {
            if(!currentState.isLoading) {
                currentState.dataState?.apply {
                    when (this) {
                        is DrivingRouteDataState -> {
                            DrivingRouteOverlayContent(data = this)
                        }
                        is BusRouteDataState -> {
                            BusRouteOverlayContent(data = this)
                        }
                        is WalkRouteDataState -> {
                            WalkRouteOverlayContent(data = this)
                        }
                        is RideRouteDataState -> {
                            RideRouteOverlayContent(data = this)
                        }
                    }
                }
            }
        }
        if(currentState.isLoading) {
            ReadCenterLoading()
        }
        MenuButtonList(viewModel::queryRoutePlan)
        RoadTrafficSwitch(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .clickable(onClick = viewModel::switchRoadTraffic),
            isEnable = currentState.mapProperties.isTrafficEnabled
        )
    }
}

@Composable
private fun BoxScope.MenuButtonList(onClick:(Int) -> Unit) {
    val currentOnClick by rememberUpdatedState(newValue = onClick)
    FlowRow(
        modifier = Modifier
            .align(Alignment.TopCenter).fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3F))
    ) {
        MapMenuButton(text = "驾车", onClick = { currentOnClick.invoke(0) })
        MapMenuButton(text = "公交", onClick = { currentOnClick.invoke(1) })
        MapMenuButton(text = "步行", onClick = { currentOnClick.invoke(2) })
        MapMenuButton(text = "骑行", onClick = { currentOnClick.invoke(3) })
    }
}