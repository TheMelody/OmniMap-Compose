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

package com.melody.bdmap.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.melody.bdmap.myapplication.R
import com.melody.bdmap.myapplication.contract.BM3DPrismContract
import com.melody.bdmap.myapplication.viewmodel.BM3DPrismViewModel
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.overlay.BM3DPrismOverlay
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.melody.sample.common.utils.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * BM3DPrismScreen
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 16:51
 */
@Composable
internal fun BM3DPrismScreen() {
    val viewModel: BM3DPrismViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = position.copy(overlook = -30F)
    }

    LaunchedEffect(Unit) {
        cameraPositionState.animate(
            MapStatusUpdateFactory.newMapStatus(
                MapStatus.Builder().target(uiState.searchLatLng).overlook(-30f).zoom(12F).build()
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is BM3DPrismContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BDMap(
            modifier = Modifier.matchParentSize(),
            uiSettings = uiState.mapUiSettings,
            properties = uiState.mapProperties,
            cameraPositionState = cameraPositionState
        ) {
            uiState.bM3DPrism?.apply {
                points?.forEach {
                    BM3DPrismOverlay(
                        height = 200F,
                        points = it,
                        topFaceColor = this.topFaceColor,
                        sideFaceColor = this.sideFaceColor,
                        customSideImage = BitmapDescriptorFactory.fromResource(R.drawable.wenli)
                    )
                }
            }
        }
    }
}