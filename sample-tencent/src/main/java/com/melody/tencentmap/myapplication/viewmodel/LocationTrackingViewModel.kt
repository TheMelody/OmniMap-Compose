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

package com.melody.tencentmap.myapplication.viewmodel

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.model.ISensorDegreeListener
import com.melody.sample.common.utils.SensorEventHelper
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
import com.melody.tencentmap.myapplication.contract.LocationTrackingContract
import com.melody.tencentmap.myapplication.repo.DragDropSelectPointRepository
import com.melody.tencentmap.myapplication.repo.LocationTrackingRepository
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.Dispatchers

/**
 * LocationTrackingViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:40
 */
class LocationTrackingViewModel :
    BaseViewModel<LocationTrackingContract.Event, LocationTrackingContract.State, LocationTrackingContract.Effect>(),
    LocationSource,TencentLocationListener, ISensorDegreeListener {

    private var mLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    private var mLocationManager: TencentLocationManager? = null
    private var mLocationRequest: TencentLocationRequest? = null
    private val sensorEventHelper = SensorEventHelper()

    override fun createInitialState(): LocationTrackingContract.State {
        return LocationTrackingContract.State(
            mapProperties = MapProperties(),
            mapUiSettings = LocationTrackingRepository.initMapUiSettings(),
            locationCircleRadius = 0F,
            currentRotation = 0F,
            isShowOpenGPSDialog = false,
            grantLocationPermission = false,
            locationLatLng = null,
            isOpenGps = null
        )
    }

    override fun handleEvents(event: LocationTrackingContract.Event) {
        when(event) {
            is LocationTrackingContract.Event.ShowOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = true) }
            }
            is LocationTrackingContract.Event.HideOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = false) }
            }
        }
    }

    init {
        sensorEventHelper.registerSensorListener(this)
        LocationTrackingRepository.initLocation { manager,request ->
            mLocationManager = manager
            mLocationRequest = request
        }
    }

    /**
     * 检查系统GPS开关是否打开
     */
    fun checkSystemGpsPermission() = asyncLaunch(Dispatchers.IO) {
        val isOpenGps = DragDropSelectPointRepository.checkGPSIsOpen()
        setState { copy(isOpenGps = isOpenGps) }
        if(!isOpenGps) {
            setEvent(LocationTrackingContract.Event.ShowOpenGPSDialog)
        } else {
            hideOpenGPSDialog()
        }
    }

    fun hideOpenGPSDialog() {
        setEvent(LocationTrackingContract.Event.HideOpenGPSDialog)
    }

    /**
     * 手机开了GPS，app没有授予权限
     */
    fun handleNoGrantLocationPermission() {
        setState { copy(grantLocationPermission = false) }
        setEvent(LocationTrackingContract.Event.ShowOpenGPSDialog)
    }

    fun handleGrantLocationPermission() {
        setState { copy(grantLocationPermission = true) }
        checkSystemGpsPermission()
        if(DragDropSelectPointRepository.checkGPSIsOpen()){
            // GPS和app的权限授权都成功，需要更新MyLocationStyle
            updateMyLocationStyle()
        }
    }

    fun openGPSPermission(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if(DragDropSelectPointRepository.checkGPSIsOpen()) {
            // 已打开系统GPS，APP还没授权，跳权限页面
            openAppPermissionSettingPage()
        } else {
            // 打开系统GPS开关页面
            launcher.safeLaunch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun updateMyLocationStyle() {
        // 因为腾讯的BitmapDescriptor需要在获取到MapContext之后才能用，否则会返回null，会导致定位蓝点图标无法修改生效
        val myLocationStyle = LocationTrackingRepository.initMyLocationStyle()
        setState {
            copy(
                // 打开定位图层，修改定位样式
                mapProperties = mapProperties.copy(
                    isMyLocationEnabled = true,
                    myLocationStyle = myLocationStyle
                ),
                // 显示定位按钮
                mapUiSettings = mapUiSettings.copy(myLocationButtonEnabled = true)
            )
        }
    }

    override fun deactivate() {
        mLocationManager?.removeUpdates(this)
        mLocationManager = null
        mLocationRequest = null
        mLocationChangedListener = null
    }

    override fun onCleared() {
        sensorEventHelper.unRegisterSensorListener()
        deactivate()
        super.onCleared()
    }

    override fun onLocationChanged(tencentLocation: TencentLocation?, errorCode: Int, s: String?) {
        LocationTrackingRepository.handleLocationChange(
            errorCode = errorCode,
            tencentLocation = tencentLocation,
            locationChangedListener = mLocationChangedListener
        ) {
            setState {
                copy(locationLatLng = LatLng(it.latitude, it.longitude),locationCircleRadius = it.accuracy)
            }
        }
    }

    override fun onStatusUpdate(s: String?, i: Int, s1: String?) {
    }

    /**
     * 当用户开启展示定位点时，SDK 会回调这个方法，并将 SDK 内部的位置监听器返回给用户，用户可以通过这个位置监听器设置定位点的坐标、精度等位置信息
     */
    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mLocationChangedListener = listener
        LocationTrackingRepository.requestLocationUpdates(
            locationManager = mLocationManager,
            locationRequest = mLocationRequest,
            locationListener = this
        ) {
            setEffect { LocationTrackingContract.Effect.Toast(it) }
        }
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(currentRotation = 360 - degree) }
    }
}