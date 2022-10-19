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
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2
import com.melody.map.myapplication.contract.DragDropSelectPointContract
import com.melody.map.myapplication.repo.DragDropSelectPointRepository
import com.melody.sample.common.base.BaseViewModel
import com.melody.sample.common.model.ISensorDegreeListener
import com.melody.sample.common.utils.SensorEventHelper
import com.melody.sample.common.utils.openAppPermissionSettingPage
import com.melody.sample.common.utils.safeLaunch
import kotlinx.coroutines.Dispatchers

/**
 * 高德地图DragDropSelectPointViewModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 09:30
 */
class DragDropSelectPointViewModel :
    BaseViewModel<DragDropSelectPointContract.Event, DragDropSelectPointContract.State, DragDropSelectPointContract.Effect>(),
    AMapLocationListener, PoiSearchV2.OnPoiSearchListener, ISensorDegreeListener {

    private var mLocationClientSingle: AMapLocationClient? = null

    // Poi查询条件类
    private var mPoiItemQuery : PoiSearchV2.Query? = null
    private var mPoiItemSearch: PoiSearchV2? = null

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
        DragDropSelectPointRepository.restartLocation(
            locationClient = mLocationClientSingle,
            listener = this@DragDropSelectPointViewModel
        ) {
            mLocationClientSingle = it
        }
    }

    fun showSelectAddressInfo(poiItemV2: PoiItemV2) {
        setEffect {
            DragDropSelectPointContract.Effect.Toast(
                "选择的地址是：".plus(poiItemV2.cityName ?: "")
                    .plus(poiItemV2.adName ?: "")
                    .plus(poiItemV2.snippet ?: "")
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
        mLocationClientSingle?.setLocationListener(null)
        mLocationClientSingle?.stopLocation()
        mLocationClientSingle?.onDestroy()
        mLocationClientSingle = null
        super.onCleared()
    }

    override fun onLocationChanged(location: AMapLocation?) {
        setState { copy(isClickForceStartLocation = false) }
        if(null == location) {
            setEffect { DragDropSelectPointContract.Effect.Toast("定位失败,请检查定位权限和网络....") }
            return
        }
        val latitude = location.latitude
        val longitude = location.longitude
        setState { copy(currentLocation = LatLng(latitude,longitude)) }
        doSearchQueryPoi(LatLng(latitude,longitude))
    }

    /**
     * 搜索当前位置附近1000米内的地址数据
     */
    fun doSearchQueryPoi(latLon: LatLng) = asyncLaunch(Dispatchers.IO) {
        DragDropSelectPointRepository.doSearchQueryPoi(
            searchV2 = mPoiItemSearch,
            moveLatLonPoint = LatLonPoint(latLon.latitude,latLon.longitude),
            listener = this@DragDropSelectPointViewModel
        ) { a, b ->
            mPoiItemQuery = a
            mPoiItemSearch = b
        }
    }

    override fun onPoiSearched(poiResult: PoiResultV2?, resultCode: Int) {
        DragDropSelectPointRepository.handlePoiSearched(mPoiItemQuery, poiResult, resultCode) {
            setState { copy(poiItems = it) }
        }
    }

    override fun onPoiItemSearched(p0: PoiItemV2?, p1: Int) {
    }

    override fun onSensorDegree(degree: Float) {
        setState { copy(currentRotation = degree) }
    }
}