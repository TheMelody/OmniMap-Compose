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

package com.melody.bdmap.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.melody.bdmap.myapplication.contract.SmoothMoveContract
import com.melody.bdmap.myapplication.viewmodel.SmoothMoveViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.PolylineCustomTexture
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.position.rememberCameraPositionState
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
    val carMarkState = rememberMarkerState()
    LaunchedEffect(currentState.bounds,currentState.isMapLoaded) {
        if(!currentState.isMapLoaded || currentState.bounds == null) return@LaunchedEffect
        // 移动并设置边距
        cameraPositionState.animate(MapStatusUpdateFactory.newLatLngBounds(currentState.bounds,200,100,200,100))
    }

    LaunchedEffect(currentState.trackMarkerPosition) {
        carMarkState.position = currentState.trackMarkerPosition
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is SmoothMoveContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = currentState.uiSettings,
            onMapLoaded = viewModel::handleMapLoaded
        ) {
            PolylineCustomTexture(
                isThined = false,
                customTexture = PolylineCustomTexture.create(
                    isKeepScale = true,
                    points = currentState.trackPoints,
                    texture = currentState.bitmapTexture
                )
            )
            currentState.movingTrackMarker?.let {
                Marker(
                    icon = it,
                    anchor = Offset(0.5F,0.5F),
                    state = carMarkState,
                    rotation = currentState.trackMarkerRotate
                )
            }
        }
        if(currentState.isMapLoaded) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3F))){
                MapMenuButton(
                    modifier = Modifier.align(Alignment.Center),
                    text = if (currentState.showPauseLabel) "暂停" else "开始",
                    onClick = viewModel::toggle
                )
            }
        }
    }
}