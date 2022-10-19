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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.CameraUpdateFactory
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.overlay.MovingPointOverlay
import com.melody.map.gd_compose.overlay.Polyline
import com.melody.map.gd_compose.position.rememberCameraPositionState
import com.melody.map.myapplication.contract.SmoothMoveContract
import com.melody.map.myapplication.viewmodel.SmoothMoveViewModel
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.MapMenuButton
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * SmoothMoveScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/12 14:15
 */
@Composable
internal fun SmoothMoveScreen() {
    val viewModel: SmoothMoveViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(currentState.trackPoints) {
        if(null == currentState.trackPoints) return@LaunchedEffect
        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(currentState.bounds, 100))
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is SmoothMoveContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = currentState.uiSettings,
            onMapLoaded = viewModel::handleMapLoaded
        ) {
            Polyline(
                points = currentState.trackPoints?: emptyList(),
                useGradient = true,
                lineCustomTexture = currentState.bitmapTexture,
                width = 18F
            )
            MovingPointOverlay(
                points = currentState.trackPoints?: emptyList(),
                descriptor = currentState.movingTrackMarker,
                isStartSmoothMove = currentState.isStart,
                totalDuration = currentState.totalDuration,
                onClick = {
                    viewModel.pointOverLayClick()
                    true
                }
            )
        }
        if(currentState.isMapLoaded) {
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3F))){
                MapMenuButton(
                    modifier = Modifier.align(Alignment.Center),
                    text = if (currentState.isStart) "暂停" else "开始",
                    onClick = viewModel::toggle
                )
            }
        }
    }
}