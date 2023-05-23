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

package com.melody.bdmap.myapplication.viewmodel

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.melody.bdmap.myapplication.contract.DragDropSelectPointContract
import com.melody.bdmap.myapplication.repo.DragDropSelectPointRepository
import com.melody.bdmap.myapplication.utils.BDMapUtils
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.model.ISensorDegreeListener
import com.melody.sample.common.utils.SensorEventHelper
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * DragDropSelectPointViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/04/26 10:44
 */
class DragDropSelectPointViewModel :
    BaseViewModel<DragDropSelectPointContract.Event, DragDropSelectPointContract.State, DragDropSelectPointContract.Effect>(),
    ISensorDegreeListener {

    private var mLocClient: LocationClient? = null
    private val sensorEventHelper = SensorEventHelper()

    override fun createInitialState(): DragDropSelectPointContract.State {
        return DragDropSelectPointContract.State(
            isForceStartLocation = false,
            isShowOpenGPSDialog = false,
            isOpenGps = null,
            currentLocation = null,
            currentRotation = 0F,
            poiItems = null
        )
    }

    override fun handleEvents(event: DragDropSelectPointContract.Event) {
        when(event) {
            is DragDropSelectPointContract.Event.ShowOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = true) }
            }
            is DragDropSelectPointContract.Event.HideOpenGPSDialog -> {
                setState { copy(isShowOpenGPSDialog = false) }
            }
        }
    }

    init {
        checkGpsStatus()
        sensorEventHelper.registerSensorListener(this)
    }

    fun checkGpsStatus() = asyncLaunch(Dispatchers.IO) {
        val isOpenGps = DragDropSelectPointRepository.checkGPSIsOpen()
        setState { copy(isOpenGps = isOpenGps) }
        if(!isOpenGps) {
            setEvent(DragDropSelectPointContract.Event.ShowOpenGPSDialog)
        } else {
            hideOpenGPSDialog()
        }
    }

    fun hideOpenGPSDialog() {
        setEvent(DragDropSelectPointContract.Event.HideOpenGPSDialog)
    }

    fun startMapLocation() = asyncLaunch(Dispatchers.IO) {
        if(currentState.isForceStartLocation) return@asyncLaunch
        setState { copy(isForceStartLocation = true) }
        if(null == mLocClient) {
            mLocClient = DragDropSelectPointRepository.initLocationClient()
            mLocClient?.registerLocationListener(mLocationListener)
            mLocClient?.start()
        } else {
            mLocClient?.restart()
        }
    }


    fun showSelectAddressInfo(poiItemData: PoiInfo) {
        setEffect {
            DragDropSelectPointContract.Effect.Toast(
                "选择的地址是：".plus(poiItemData.name ?: "")
                    .plus(poiItemData.address ?: "")
            )
        }
    }

    /**
     * 手机开了GPS，app没有授予权限
     */
    fun handleNoGrantLocationPermission() {
        setEvent(DragDropSelectPointContract.Event.ShowOpenGPSDialog)
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

    override fun onCleared() {
        sensorEventHelper.unRegisterSensorListener()
        mLocClient?.unRegisterLocationListener(mLocationListener)
        mLocClient?.stop()
        mLocClient = null
        super.onCleared()
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(currentRotation = degree) }
    }

    private val mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            if(null != bdLocation) {
                val checkErrorMsg = BDMapUtils.locationErrorMessage(bdLocation.locType)
                if(checkErrorMsg == null) {
                    val isFirst = currentState.currentLocation == null
                    // 设置定位数据
                    setState {
                        copy(currentLocation = LatLng(bdLocation.latitude, bdLocation.longitude))
                    }
                    if(isFirst) {
                        doSearchQueryPoi(latLng = LatLng(bdLocation.latitude, bdLocation.longitude))
                    }
                } else {
                    // 定位出错了
                    setEffect { DragDropSelectPointContract.Effect.Toast(checkErrorMsg) }
                }
            } else {
                setEffect { DragDropSelectPointContract.Effect.Toast("定位失败,请检查定位权限和网络....") }
            }
        }
    }


    /**
     * 搜索当前位置附近1000米内的地址数据
     */
    fun doSearchQueryPoi(latLng: LatLng) = asyncLaunch(Dispatchers.IO) {
        DragDropSelectPointRepository.queryPoiResult(
            latLng = latLng,
            onSuccess = {
                setState { copy(poiItems = it, isForceStartLocation = false) }
            },
            onFailed = {
                setEffect { DragDropSelectPointContract.Effect.Toast(it) }
                setState { copy(isForceStartLocation = false) }
            })
    }
}