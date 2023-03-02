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

package com.melody.tencentmap.myapplication.ui

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.overlay.Circle
import com.melody.map.tencent_compose.overlay.Marker
import com.melody.map.tencent_compose.overlay.rememberMarkerState
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import com.melody.ui.components.R
import com.melody.tencentmap.myapplication.contract.LocationTrackingContract
import com.melody.tencentmap.myapplication.dialog.ShowOpenGPSDialog
import com.melody.tencentmap.myapplication.viewmodel.LocationTrackingViewModel
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * 高德地图默认定位蓝点，这里我们代码动态替换了SDK默认的蓝点图片
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:31
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationTrackingScreen() {
    val viewModel: LocationTrackingViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    var isRenderLocation by rememberSaveable{ mutableStateOf(false) }
    val cameraPosition = rememberCameraPositionState()

    val locationIconState = rememberMarkerState(position = currentState.locationLatLng?: LatLng(0.0,0.0))

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is LocationTrackingContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkSystemGpsPermission)
    val reqGPSPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onGrantAllPermission = viewModel::handleGrantLocationPermission,
        onNoGrantPermission = viewModel::handleNoGrantLocationPermission
    )

    LaunchedEffect(Unit) {
        snapshotFlow { reqGPSPermission.allPermissionsGranted }.collect {
            // 从app应用权限开关页面，打开权限，需要再检查一下GPS开关
            viewModel.checkSystemGpsPermission()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { currentState.locationLatLng }.collect { latLng ->
            latLng?.let {
                locationIconState.position = it
                if(!isRenderLocation) {
                    isRenderLocation = true
                    // 确保首次需要动画位移到当前用户的位置
                    cameraPosition.move(CameraUpdateFactory.newLatLng(it))
                }
            }
        }
    }

    LaunchedEffect(currentState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(currentState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            } else {
                viewModel.handleGrantLocationPermission()
            }
        }
    }

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TXMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPosition,
            properties = currentState.mapProperties,
            uiSettings = currentState.mapUiSettings,
            locationSource = viewModel
        ){
            // 【建议】：不使用有角度方向的图标，使用圆形图标加阴影效果比这个好多了
            // 这里不用腾讯自己的定位蓝点样式
            if(locationIconState.position.latitude > 0){
                Circle(
                    center = locationIconState.position,
                    fillColor = Color(0x801A9CE2),
                    strokeColor = Color(0xFF1A9CE2),
                    strokeWidth = 1F,
                    radius = currentState.locationCircleRadius.toDouble()
                )
                Marker(
                    anchor = Offset(0.5F,0.5F),
                    rotation = currentState.currentRotation,
                    state = locationIconState,
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_self)
                )
            }
        }
    }
}