package com.melody.bdmap.myapplication.utils

import android.content.Context
import com.baidu.location.LocationClient
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
        SDKInitializer.setAgreePrivacy(context, true)
        LocationClient.setAgreePrivacy(true)
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

    fun locationErrorMessage(locationType: Int): String? {
        return when(locationType) {
            62 -> "无法获取有效定位依据，定位失败，请检查运营商网络或者WiFi网络是否正常开启，尝试重新请求定位。"
            63 -> "网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。"
            67 -> "离线定位失败，请检查网络。"
            68 -> "网络连接失败时，查找本地离线定位时对应的返回结果。"
            162 -> "请求串密文解析失败，一般是由于客户端SO文件加载失败造成，请严格参照开发指南或demo开发，放入对应SO文件。"
            167 -> "服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。"
            505 -> "AK不存在或者非法，请按照说明文档重新申请AK。"
            else -> null
        }
    }
}