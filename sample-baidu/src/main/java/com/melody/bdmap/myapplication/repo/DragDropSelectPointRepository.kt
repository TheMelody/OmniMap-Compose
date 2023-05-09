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

package com.melody.bdmap.myapplication.repo

import android.content.Context
import android.location.LocationManager
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.melody.sample.common.utils.SDKUtils
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * DragDropSelectPointRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/04/26 11:04
 */
object DragDropSelectPointRepository {

    fun checkGPSIsOpen(): Boolean {
        val locationManager: LocationManager? = SDKUtils.getApplicationContext()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
    }

    fun initLocationClient(): LocationClient {
        return LocationClient(SDKUtils.getApplicationContext()).apply {
            val clientOption = LocationClientOption()
            // 可选，默认false，设置是否开启卫星定位
            clientOption.isOpenGnss = true
            // 可选，默认gcj02，设置返回的定位结果坐标系
            clientOption.setCoorType("gcj02")
            // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            clientOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            // 设置发起定位请求的间隔时间
            clientOption.setScanSpan(1000)
            // 返回的定位结果包含地址信息
            clientOption.setIsNeedAddress(true)
            // 可选，默认false，设置是否收集CRASH信息，默认收集
            clientOption.SetIgnoreCacheException(false)
            // 可选，默认false，设置是否当卫星定位有效时按照1S1次频率输出卫星定位结果
            clientOption.isLocationNotify = true
            // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
            clientOption.setOpenAutoNotifyMode()
            // 返回的定位结果包含手机机头的方向
            clientOption.setNeedDeviceDirect(true)
            // 需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            locOption = clientOption
        }
    }

    /**
     * 查询附近1000米范围内的数据
     */
    suspend fun queryPoiResult(
        latLng: LatLng,
        onSuccess: (List<PoiInfo>) -> Unit,
        onFailed: (String?) -> Unit
    ) {
        // 先通过逆地址编码解析
        val geo2AddressResult = kotlin.runCatching {
            geo2Address(latLng)
        }
        val result = geo2AddressResult.getOrNull()
        if (geo2AddressResult.isSuccess) {
            if(null != result) {
                // 排序
                result.poiList?.sortBy { it.distance }
                onSuccess.invoke(result.poiList?: emptyList())
            } else {
                onSuccess.invoke(emptyList())
            }
        } else {
            onFailed.invoke(geo2AddressResult.exceptionOrNull()?.message)
            return
        }
    }

    /**
     * 逆地址编码
     */
    private suspend fun geo2Address(latLng: LatLng): ReverseGeoCodeResult {
        return suspendCancellableCoroutine { continuation ->
            val reverseGeoCodeOption = ReverseGeoCodeOption()
                .location(latLng) // 设置反地理编码位置坐标
                .radius(1000) //  POI召回半径，允许设置区间为0-1000米，超过1000米按1000米召回。默认值为1000
                .pageSize(30)
                .pageNum(0)
            val bdSearch = GeoCoder.newInstance()
            bdSearch.setOnGetGeoCodeResultListener(object :OnGetGeoCoderResultListener {
                override fun onGetGeoCodeResult(p0: GeoCodeResult?) {
                }
                override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult?) {
                    bdSearch.destroy()
                    if (null == result) {
                        continuation.resumeWith(Result.failure(NullPointerException()))
                        return
                    }
                    continuation.resumeWith(Result.success(result))
                }
            })
            //  发起反地理编码请求，该方法必须【在监听之后执行】，否则会在某些场景出现拿不到回调结果的情况
            bdSearch.reverseGeoCode(reverseGeoCodeOption)
        }
    }
}