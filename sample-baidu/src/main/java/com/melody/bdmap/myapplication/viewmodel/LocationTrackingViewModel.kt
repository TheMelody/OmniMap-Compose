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

package com.melody.bdmap.myapplication.viewmodel

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.mapapi.model.LatLng
import com.melody.bdmap.myapplication.contract.LocationTrackingContract
import com.melody.bdmap.myapplication.repo.LocationTrackingRepository
import com.melody.bdmap.myapplication.utils.BDMapUtils
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.model.ISensorDegreeListener
import com.melody.sample.common.utils.SensorEventHelper
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
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
    ISensorDegreeListener {

    private var mLocClient: LocationClient? = null
    private val sensorEventHelper = SensorEventHelper()

    override fun createInitialState(): LocationTrackingContract.State {
        return LocationTrackingContract.State(
            mapProperties = LocationTrackingRepository.initMapProperties(),
            mapUiSettings = LocationTrackingRepository.initMapUiSettings(),
            isForceLocation = true,
            isShowOpenGPSDialog = false,
            sensorDegree = 0F,
            grantLocationPermission = false,
            locationLatLng = null,
            locationSource = null,
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
    }

    /**
     * 检查系统GPS开关是否打开
     */
    fun checkGpsStatus() = asyncLaunch(Dispatchers.IO) {
        val isOpenGps = LocationTrackingRepository.checkGPSIsOpen()
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
        if(LocationTrackingRepository.checkGPSIsOpen()) {
            // 已打开系统GPS，APP还没授权，跳权限页面
            openAppPermissionSettingPage()
        } else {
            // 打开系统GPS开关页面
            launcher.safeLaunch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    fun startMapLocation() {
        setState { copy(isForceLocation = true) }
        if(null == mLocClient) {
            mLocClient = LocationTrackingRepository.initLocationClient()
            mLocClient?.registerLocationListener(mLocationListener)
            mLocClient?.start()
        } else {
            mLocClient?.restart()
        }
    }

    private val mLocationListener = object : BDAbstractLocationListener() {
        /*override fun onLocDiagnosticMessage(p0: Int, p1: Int, p2: String?) {
            super.onLocDiagnosticMessage(p0, p1, p2)
            Log.d("Location",">>>p0:"+p0+",p1:"+p1+",p2:"+p2)
        }*/
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            if(null != bdLocation) {
                val checkErrorMsg = BDMapUtils.locationErrorMessage(bdLocation.locType)
                if(checkErrorMsg == null) {
                    // 设置定位数据
                    val locationData = LocationTrackingRepository.bDLocation2MyLocation(
                        bdLocation,
                        currentState.sensorDegree
                    )
                    setState {
                        copy(
                            locationSource = locationData,
                            locationLatLng = LatLng(bdLocation.latitude, bdLocation.longitude),
                            isForceLocation = false
                        )
                    }
                } else {
                    // 定位出错了
                    setEffect { LocationTrackingContract.Effect.Toast(checkErrorMsg) }
                }
            }
        }
    }

    override fun onCleared() {
        sensorEventHelper.unRegisterSensorListener()
        mLocClient?.unRegisterLocationListener(mLocationListener)
        mLocClient?.stop()
        mLocClient = null
        super.onCleared()
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(sensorDegree = 360 - degree) }
    }
}