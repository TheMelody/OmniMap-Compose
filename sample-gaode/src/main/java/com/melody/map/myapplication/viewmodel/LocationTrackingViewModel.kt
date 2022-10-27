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

package com.melody.map.myapplication.viewmodel

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.LocationSource
import com.amap.api.maps.LocationSource.OnLocationChangedListener
import com.amap.api.maps.model.LatLng
import com.melody.map.myapplication.contract.LocationTrackingContract
import com.melody.map.myapplication.repo.LocationTrackingRepository
import com.melody.map.myapplication.repo.DragDropSelectPointRepository
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * LocationTrackingViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:40
 */
class LocationTrackingViewModel :
    BaseViewModel<LocationTrackingContract.Event, LocationTrackingContract.State, LocationTrackingContract.Effect>(),
    LocationSource,
    AMapLocationListener {

    private var mListener: OnLocationChangedListener? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null

    override fun createInitialState(): LocationTrackingContract.State {
        return LocationTrackingContract.State(
            mapProperties = LocationTrackingRepository.initMapProperties(),
            mapUiSettings = LocationTrackingRepository.initMapUiSettings(),
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

    /**
     * 检查系统GPS开关是否打开
     */
    fun checkGpsStatus() = asyncLaunch(Dispatchers.IO) {
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
        checkGpsStatus()
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

    fun startMapLocation() {
        LocationTrackingRepository.initAMapLocationClient(mLocationClient,this) { client, option->
            mLocationClient = client
            mLocationOption = option
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        LocationTrackingRepository.handleLocationChange(amapLocation) { aMapLocation, msg ->
            if(null != aMapLocation) {
                val delayTime = if(null == currentState.locationLatLng) 100L else 0L
                setState {
                    copy(locationLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude))
                }
                asyncLaunch {
                    // 首次直接显示，高德地图【默认小蓝点】会【有点闪烁】，延迟一下再回调
                    delay(delayTime)
                    // 显示系统小蓝点
                    mListener?.onLocationChanged(aMapLocation)
                }
            } else {
                setEffect { LocationTrackingContract.Effect.Toast(msg) }
            }
        }
    }

    override fun activate(listener: OnLocationChangedListener?) {
        mListener = listener
        if(DragDropSelectPointRepository.checkGPSIsOpen() && currentState.grantLocationPermission) {
            startMapLocation()
        }
    }

    override fun deactivate() {
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
        mListener = null
    }

    override fun onCleared() {
        mLocationClient?.onDestroy()
        mLocationClient = null
        super.onCleared()
    }
}