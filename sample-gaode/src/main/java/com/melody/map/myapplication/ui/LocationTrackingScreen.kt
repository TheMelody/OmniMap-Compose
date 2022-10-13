package com.melody.map.myapplication.ui

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.position.rememberCameraPositionState
import com.melody.map.myapplication.contract.LocationTrackingContract
import com.melody.map.myapplication.dialog.ShowOpenGPSDialog
import com.melody.map.myapplication.viewmodel.LocationTrackingViewModel
import com.melody.sample.common.launcher.handlerGPSLauncher
import com.melody.sample.common.utils.requestMultiplePermission
import com.melody.sample.common.utils.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * 高德地图默认定位蓝点，这里我们代码动态替换了SDK默认的蓝点图片
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 17:31
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun LocationTrackingScreen() {
    val viewModel: LocationTrackingViewModel = viewModel()
    val currentState by viewModel.uiState.collectAsState()
    val cameraPosition = rememberCameraPositionState {
        // 不预加载显示默认北京的位置
        position = CameraPosition(LatLng(0.0, 0.0), 11f, 0f, 0f)
    }
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.onEach {
            if(it is LocationTrackingContract.Effect.Toast) {
                showToast(it.msg)
            }
        }.collect()
    }

    val openGpsLauncher = handlerGPSLauncher(viewModel::checkGpsStatus)
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
            viewModel.checkGpsStatus()
        }
    }

    LaunchedEffect(currentState.locationLatLng) {
        if(null == currentState.locationLatLng) return@LaunchedEffect
        cameraPosition.move(CameraUpdateFactory.newLatLng(currentState.locationLatLng))
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

    if(currentState.isShowOpenGPSDialog) {
        ShowOpenGPSDialog(
            onDismiss = viewModel::hideOpenGPSDialog,
            onPositiveClick = {
                viewModel.openGPSPermission(openGpsLauncher)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GDMap(
            cameraPositionState = cameraPosition,
            properties = currentState.mapProperties,
            uiSettings = currentState.mapUiSettings,
            locationSource = viewModel,
            onMapLoaded = viewModel::checkGpsStatus
        )
    }
}