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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.tencent_compose.TXMap
import com.melody.map.tencent_compose.overlay.Marker
import com.melody.map.tencent_compose.overlay.rememberMarkerState
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import com.melody.tencentmap.myapplication.contract.DragDropSelectPointContract
import com.melody.tencentmap.myapplication.dialog.ShowOpenGPSDialog
import com.melody.tencentmap.myapplication.viewmodel.DragDropSelectPointViewModel
import com.melody.ui.components.ForceStartLocationButton
import com.melody.ui.components.UIMarkerInScreenCenter
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * DragDropSelectPointContract
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/17 09:14
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun DragDropSelectPointScreen() {
    var isMapLoaded by rememberSaveable{ mutableStateOf(false) }
    val dragDropAnimatable = remember { Animatable(Size.Zero,Size.VectorConverter) }
    val cameraPositionState = rememberCameraPositionState()
    val locationState = rememberMarkerState()
    val viewModel: DragDropSelectPointViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)

    val reqGPSPermission = requestMultiplePermission(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onNoGrantPermission = viewModel::handleNoGrantLocationPermission,
        onGrantAllPermission = viewModel::checkGpsStatus
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is DragDropSelectPointContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { reqGPSPermission.allPermissionsGranted }.collect {
            viewModel.checkGpsStatus()
        }
    }

    LaunchedEffect(currentState.isOpenGps, reqGPSPermission.allPermissionsGranted) {
        if(currentState.isOpenGps == true) {
            if (!reqGPSPermission.allPermissionsGranted) {
                reqGPSPermission.launchMultiplePermissionRequest()
            } else {
                viewModel.startMapLocation()
            }
        }
    }

    // 地图移动，中心的Marker需要动画跳动
    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            dragDropAnimatable.animateTo(Size(45F,20F))
        } else {
            dragDropAnimatable.animateTo(Size(25F,11F))
            // 查询附近1000米地址数据
            viewModel.doSearchQueryPoi(cameraPositionState.position.latlng,"")
        }
    }

    LaunchedEffect(currentState.isClickForceStartLocation, currentState.currentLocation) {
        val curLocation = currentState.currentLocation
        if(null == curLocation || cameraPositionState.position.latlng == curLocation) return@LaunchedEffect
        locationState.position = curLocation
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(currentState.currentLocation, 17F))
    }

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5F)) {
            TXMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    isZoomGesturesEnabled = true,
                    isScrollGesturesEnabled = true,
                ),
                onMapLoaded = {
                    isMapLoaded = true
                }
            ){
                Marker(
                    state = locationState,
                    anchor = Offset(0.5F,0.5F),
                    rotation = currentState.currentRotation,
                    icon = BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_location_self),
                    onClick = { true }
                )
            }
            if(isMapLoaded) {
                // 地图加载出来之后，再显示出来选点的图标
                UIMarkerInScreenCenter(com.melody.tencentmap.myapplication.R.drawable.purple_pin) {
                    dragDropAnimatable.value
                }
            }
            // 强制触发单次定位的按钮
            ForceStartLocationButton(viewModel::startMapLocation)
        }

        //选点获取到的结果
        DragDropPoiResultList(
            poiItemList = currentState.poiItems,
            onItemClick = viewModel::showSelectAddressInfo
        )
    }
}