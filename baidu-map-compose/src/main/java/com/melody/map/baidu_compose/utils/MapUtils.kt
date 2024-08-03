package com.melody.map.baidu_compose.utils

import android.content.Context
import android.util.Log
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.common.BaiduMapSDKException

object MapUtils {
    private const val TAG = "MapUtils"

    fun init(context: Context){
        try {
            // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
            SDKInitializer.initialize(context)
        } catch (ignore: BaiduMapSDKException) {
            // ignore
        }
        // 包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标，这里默认设置成国测局坐标
        SDKInitializer.setCoordType(CoordType.GCJ02)
    }

    fun updateCoordType(coordType: CoordType) {
        SDKInitializer.setCoordType(coordType)
    }

    fun setMapPrivacy(context: Context,isAgree: Boolean) {
        SDKInitializer.setAgreePrivacy(context, isAgree)
        try {
            // 获取TencentLocationManager类的Class对象
            val clazz = Class.forName("com.baidu.location.LocationClient")
            // 获取setUserAgreePrivacy方法
            val method =
                clazz.getDeclaredMethod("setAgreePrivacy", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(null, isAgree) // 此方法是静态方法
        } catch (e: Exception) {
            Log.e(TAG, "LocationClient#setAgreePrivacy:" + e.message)
        }
        Log.d(TAG, "Privacy agreement set successfully.")
        init(context)
    }
}