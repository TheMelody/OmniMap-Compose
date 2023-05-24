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

package com.melody.bdmap.myapplication.repo

import android.content.Context
import android.graphics.BitmapFactory
import android.location.LocationManager
import androidx.compose.ui.graphics.toArgb
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.sample.common.utils.SDKUtils
import com.melody.ui.components.R

/**
 * LocationTrackingRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/10 17:50
 */
object LocationTrackingRepository {

    fun checkGPSIsOpen(): Boolean {
        val locationManager: LocationManager? = SDKUtils.getApplicationContext()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
    }

    fun initMapProperties() : MapProperties {
        val iconBitmap = BitmapFactory.decodeResource(SDKUtils.getApplicationContext().resources, R.drawable.ic_map_location_self)
        val locationIcon = BitmapDescriptorFactory.fromBitmap(iconBitmap)
        return MapProperties(
            isMyLocationEnabled = true,
            myLocationStyle = MyLocationConfiguration(
                // 更新定位数据时不对地图做任何操作
                MyLocationConfiguration.LocationMode.NORMAL,
                true,
                // 修改默认小蓝点的图标
                locationIcon,
                // 设置圆形的填充颜色
                androidx.compose.ui.graphics.Color(0x80DAA217).toArgb(),
                // 设置圆形的边框颜色
                androidx.compose.ui.graphics.Color(0xAA4453B4).toArgb()
            )
        )
    }

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isZoomEnabled = true,
            isScaleControlsEnabled = true,
            isDoubleClickZoomEnabled = true,
            isScrollGesturesEnabled = true,
        )
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

    fun bDLocation2MyLocation(bdLocation: BDLocation, degree: Float): MyLocationData {
        return MyLocationData.Builder()
            .accuracy(bdLocation.radius)// 设置定位数据的精度信息，单位：米
            .direction(degree) //bdLocation.direction) // 此处设置开发者获取到的方向信息，顺时针0-360
            .latitude(bdLocation.latitude)
            .longitude(bdLocation.longitude)
            .build()
    }
}