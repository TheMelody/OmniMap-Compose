package com.melody.bdmap.myapplication.utils

import android.content.Context
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.common.BaiduMapSDKException

/**
 * BDMapUtils
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/07 17:44
 */
object BDMapUtils {

    fun updateMapViewPrivacy(context: Context) {
        SDKInitializer.setAgreePrivacy(context, false)
    }

    fun initConfig(context: Context){
        try {
            // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
            SDKInitializer.initialize(context)
        } catch (ignore: BaiduMapSDKException) {
            // ignore
        }
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标，这里设置成国测局坐标
        SDKInitializer.setCoordType(CoordType.GCJ02)
    }
}