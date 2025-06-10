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
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.model.ISensorDegreeListener
import com.melody.sample.common.utils.SDKUtils
import com.melody.sample.common.utils.SensorEventHelper
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
import com.melody.tencentmap.myapplication.contract.DragDropSelectPointContract
import com.melody.tencentmap.myapplication.repo.DragDropSelectPointRepository
import com.tencent.lbssearch.`object`.result.SearchResultObject
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import java.util.UUID

/**
 * DragDropSelectPointViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/02/17 09:16
 */
class DragDropSelectPointViewModel :
    BaseViewModel<DragDropSelectPointContract.Event, DragDropSelectPointContract.State, DragDropSelectPointContract.Effect>(),
    TencentLocationListener, ISensorDegreeListener {

    private val sensorEventHelper = SensorEventHelper()

    override fun createInitialState(): DragDropSelectPointContract.State {
        return DragDropSelectPointContract.State(
            isClickForceStartLocation = false,
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
        // 这里，可以在应用初始化的时候，缓存下来，下次直接使用，保证唯一性即可，毕竟获取手机通讯录权限是隐私权限
        // 上传设备唯一标识，用于在定位发生问题查询问题原因
        TencentLocationManager.getInstance(SDKUtils.getApplicationContext())
            .setDeviceID(SDKUtils.getApplicationContext(), "id" + UUID.randomUUID())
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
        if(currentState.isClickForceStartLocation) return@asyncLaunch
        setState { copy(isClickForceStartLocation = true) }
        // 单次定位，不需要主动调用停止定位
        TencentLocationManager.getInstance(SDKUtils.getApplicationContext())
            .requestSingleFreshLocation(null, this@DragDropSelectPointViewModel, Looper.getMainLooper())
    }

    fun showSelectAddressInfo(poiItemData: SearchResultObject.SearchResultData) {
        setEffect {
            DragDropSelectPointContract.Effect.Toast(
                "选择的地址是：".plus(poiItemData.title ?: "")
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
        if(currentState.isClickForceStartLocation) {
            TencentLocationManager.getInstance(SDKUtils.getApplicationContext()).removeUpdates(this)
        }
        super.onCleared()
    }

    /**
     * 搜索当前位置附近1000米内的地址数据
     */
    fun doSearchQueryPoi(latLng: LatLng, cityName: String?) = asyncLaunch(Dispatchers.IO) {
        DragDropSelectPointRepository.queryPoiResult(
            latLng = latLng,
            cityName = cityName?: "",
            onSuccess = {
                setState { copy(poiItems = it) }
            },
            onFailed = {
                setEffect { DragDropSelectPointContract.Effect.Toast(it) }
            })
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(currentRotation = 360 - degree) }
    }

    override fun onLocationChanged(location: TencentLocation?, p1: Int, p2: String?) {
        setState { copy(isClickForceStartLocation = false) }
        if (null == location) {
            setEffect { DragDropSelectPointContract.Effect.Toast("定位失败,请检查定位权限和网络....") }
            return
        }
        val latitude = location.latitude
        val longitude = location.longitude
        setState { copy(currentLocation = LatLng(latitude, longitude)) }
        doSearchQueryPoi(LatLng(latitude, longitude),location.city)
    }

    override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {
        // ignore
    }
}