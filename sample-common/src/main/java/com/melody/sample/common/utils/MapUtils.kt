package com.melody.sample.common.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin

/**
 * 腾讯地图和高德地图的经度纬度不用换算，只有百度地图需要转换一下
 * MapUtils
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/08 16:20
 */

private const val GAO_DE_MAP_PACKAGE_NAME = "com.autonavi.minimap"

private const val BAIDU_MAP_PACKAGE_NAME = "com.baidu.BaiduMap"

private const val TAG = "MapUtils"

/**
 * 打开地图App
 */
fun Context.startMapApp(dstLat: Double, dstLon: Double, dstName: String) {
    if (isInstallApp(GAO_DE_MAP_PACKAGE_NAME)) {
        openGaoDeMap(dstLat, dstLon, dstName)
    } else if (isInstallApp(BAIDU_MAP_PACKAGE_NAME)) {
        openBaiduMap(dstLat, dstLon)
    } else {
        openTencentMap(dstLat, dstLon, dstName)
    }
}

/*
 * 打开高德地图
 * @param dstLat  终点纬度
 * @param dstLon  终点经度
 * @param dstName 终点名称
 */
private fun Context.openGaoDeMap(dstLat: Double, dstLon: Double, dstName: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // 将功能Scheme以URI的方式传入data
        data = Uri.parse(
            String.format(
                "androidamap://route?" +
                "sourceApplication=%s" +
                "&sname=我的位置&dlat=%s" +
                "&dlon=%s" +
                "&dname=%s&dev=0&m=0&t=1",
                getAppName(packageName),
                dstLat,
                dstLon,
                dstName
            )
        )
    }
    applicationContext.safeStartActivity(intent)
}

/**
 * 打开百度地图
 */
private fun Context.openBaiduMap(dstLat: Double, dstLon: Double) {
    val intent = Intent().apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val latLonArray = convertGTMapLatLonToBaidu(dstLat, dstLon)
        data = Uri.parse(
            String.format(
                "baidumap://map/direction?region=0" +
                "&destination=%s,%s" +
                "&mode=transit&src=andr.waimai.%s",
                latLonArray.getOrNull(0)?: 0.0,
                latLonArray.getOrNull(1)?: 0.0,
                getAppName(packageName)
            )
        )
    }
    applicationContext.safeStartActivity(intent)
}

/*
 * 打开腾讯地图，腾讯地图和高德地图的经度纬度不用换算
 * @param dstLat  终点纬度
 * @param dstLon  终点经度
 * @param dstName 终点名称
 */
private fun Context.openTencentMap(dstLat: Double, dstLon: Double, dstName: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.parse(
            String.format(
                "qqmap://map/routeplan?type=bus&from=我的位置&fromcoord=0,0" +
                "&to=%s" +
                "&tocoord=%s,%s" +
                "&policy=1" +
                "&referer=%s",
                dstName,
                dstLat,
                dstLon,
                getAppName(packageName)
            )
        )
    }
    applicationContext.safeStartActivity(intent)
}

/**
 * 高德地图|腾讯地图（火星坐标）转换成百度地图坐标
 */
private fun convertGTMapLatLonToBaidu(lat: Double, lon: Double): Array<Double> {
    val x: Double = lon
    val y: Double = lat
    val z: Double = kotlin.math.sqrt(x * x + y * y) + 0.00002 * sin(y * Math.PI * 3000.0 / 180.0)
    val theta: Double = kotlin.math.atan2(y, x) + 0.000003 * cos(x * Math.PI * 3000.0 / 180.0)

    val convertLon =
        BigDecimal(z * cos(theta) + 0.0065).setScale(6, RoundingMode.HALF_UP).toDouble()

    val convertLat =
        BigDecimal(z * sin(theta) + 0.006).setScale(6, RoundingMode.HALF_UP).toDouble()

    return arrayOf(convertLat, convertLon)
}

internal fun Context.safeStartActivity(intent: Intent) {
    val result = runCatching {
        startActivity(intent)
    }
    if (result.isFailure) {
        Log.e(TAG, result.exceptionOrNull()?.message ?: "")
    }
}

internal fun Context.getApplicationInfo(packageName: String): ApplicationInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getApplicationInfo(
            packageName, PackageManager.ApplicationInfoFlags.of(
                PackageManager.GET_META_DATA.toLong()
            )
        )
    } else {
        packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    }
}

internal fun Context.isInstallApp(packageName: String): Boolean {
    return kotlin.runCatching {
        getApplicationInfo(packageName)
    }.isSuccess
}

internal fun Context.getAppName(packageName: String): CharSequence {
    val applicationResult = kotlin.runCatching {
        getApplicationInfo(packageName)
    }
    return applicationResult.getOrNull()?.loadLabel(packageManager) ?: packageName
}