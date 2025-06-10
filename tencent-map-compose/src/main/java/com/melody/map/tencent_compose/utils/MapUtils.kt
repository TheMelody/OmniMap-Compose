package com.melody.map.tencent_compose.utils

import android.content.Context
import android.util.Log
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer

object MapUtils {
    private const val TAG = "MapUtils"

    fun setMapPrivacy(context:Context,isAgree: Boolean) {
        TencentMapInitializer.setAgreePrivacy(context,isAgree)
        TencentMapInitializer.start(context)
        try {
            // 获取TencentLocationManager类的Class对象
            val clazz = Class.forName("com.tencent.map.geolocation.TencentLocationManager")
            // 获取setUserAgreePrivacy方法
            val method =
                clazz.getDeclaredMethod("setUserAgreePrivacy", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(null, isAgree) // 此方法是静态方法
        } catch (e: Exception) {
            Log.e(TAG, "TencentLocationManager#setUserAgreePrivacy:" + e.message)
        }
        Log.d(TAG, "Privacy agreement set successfully.")
    }
}