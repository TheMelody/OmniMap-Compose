package com.melody.map.compose.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.amap.api.maps.MapsInitializer
import java.math.BigDecimal

/**
 * MapUtils
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/8/26 15:20
 */
/**
 * 更新地图的隐私合规，【不调用地图无法正常显示】只要用到地图SDK的地方，都要先调用它
 */
fun Context.updateMapViewPrivacy() {
    MapsInitializer.updatePrivacyShow(applicationContext, true, true)
    MapsInitializer.updatePrivacyAgree(applicationContext, true)
}

/**
 * 打开系统地图app
 */
fun Context.openSystemMapApp(dlat: Double, dlon: Double, dname: String) {
    if (isInstall("com.autonavi.minimap")) {
        openGaoDeMap(dlat, dlon, dname)
    } else if (isInstall("com.baidu.BaiduMap")) {
        openBaiduMap(dlat, dlon)
    } else {
        openTencentMap(dlat, dlon, dname)
    }
}

/*
 * 打开高德地图
 * @param dlat  终点纬度
 * @param dlon  终点经度
 * @param dname 终点名称
 */
private fun Context.openGaoDeMap(dlat: Double, dlon: Double, dname: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        addCategory(Intent.CATEGORY_DEFAULT)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //将功能Scheme以URI的方式传入data
        data =
            Uri.parse(
                "androidamap://route?sourceApplication=" + getAppName(packageName)
                        + "&sname=我的位置&dlat=" + dlat
                        + "&dlon=" + dlon
                        + "&dname=" + dname
                        + "&dev=0&m=0&t=1"
            )
    }
    runCatching {
        //启动该页面即可
        applicationContext.startActivity(intent)
    }
}

/**
 * 打开百度地图
 */
private fun Context.openBaiduMap(dlat: Double, dlon: Double) {
    val intent = Intent().apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.parse(
            "baidumap://map/direction?region=0&destination="
                    + convertGDMapLatToBaidu(dlon, dlat) + ","
                    + convertGDMapLonToBaidu(dlon, dlat)
                    // 这里的src格式：(ios或者andr).companyName.appName
                    + "&mode=transit&src=andr.waimai." + getAppName(packageName)
        )
    }
    runCatching {
        //启动该页面即可
        applicationContext.startActivity(intent)
    }
}

/*
 * 打开腾讯地图
 * @param dlat  终点纬度
 * @param dlon  终点经度
 * @param dname 终点名称
 */
private fun Context.openTencentMap(dlat: Double, dlon: Double, dname: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.parse(
            "qqmap://map/routeplan?type=bus&from=我的位置&fromcoord=0,0"
                    + "&to=" + dname
                    + "&tocoord=" + dlat + "," + dlon
                    + "&policy=1&referer=" + getAppName(packageName)
        )
    }
    runCatching {
        applicationContext.startActivity(intent)
    }
}

/**
 * 高德地图（火星坐标）经度转换成百度地图经度
 */
private fun convertGDMapLonToBaidu(lon: Double, lat: Double): Double {
    val x: Double = lon
    val y: Double = lat
    val z: Double =
        kotlin.math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * 3.14159265358979324 * 3000.0 / 180.0)
    val theta: Double = kotlin.math.atan2(
        y,
        x
    ) + 0.000003 * kotlin.math.cos(x * 3.14159265358979324 * 3000.0 / 180.0)

    // lon
    return BigDecimal(z * Math.cos(theta) + 0.0065).setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
}

/**
 * 高德地图（火星坐标）纬度转换成百度地图纬度
 */
private fun convertGDMapLatToBaidu(lon: Double, lat: Double): Double {
    val x: Double = lon
    val y: Double = lat
    val z: Double =
        kotlin.math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * 3.14159265358979324 * 3000.0 / 180.0)
    val theta: Double = kotlin.math.atan2(
        y,
        x
    ) + 0.000003 * kotlin.math.cos(x * 3.14159265358979324 * 3000.0 / 180.0)

    //lat
    return BigDecimal(z * Math.sin(theta) + 0.006).setScale(6, BigDecimal.ROUND_HALF_UP).toDouble()
}

private fun Context.isInstall(packageName:String): Boolean {
    val applicationResult = kotlin.runCatching {
        packageManager.getApplicationInfo(packageName,0)
    }
    return applicationResult.isSuccess
}

private fun Context.getAppName(packageName: String): CharSequence {
    val applicationResult = kotlin.runCatching {
        packageManager.getApplicationInfo(packageName,0)
    }
    return applicationResult.getOrNull()?.loadLabel(packageManager) ?: packageName
}