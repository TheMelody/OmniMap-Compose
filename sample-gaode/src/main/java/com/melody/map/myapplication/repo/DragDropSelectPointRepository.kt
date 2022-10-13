package com.melody.map.myapplication.repo

import android.content.Context
import android.location.LocationManager
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.poisearch.PoiSearchV2
import com.melody.sample.common.utils.SDKUtils

/**
 * DragDropSelectPointRepository
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 09:43
 */
object DragDropSelectPointRepository {

    fun checkGPSIsOpen(): Boolean {
        val locationManager: LocationManager? = SDKUtils.getApplicationContext()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?: false
    }

    inline fun restartLocation(locationClient: AMapLocationClient?, listener: AMapLocationListener, block: (AMapLocationClient) -> Unit) {
        // 必须先停止，再重新初始化，否则会报错：【用户MD5安全码不通过】
        locationClient?.setLocationListener(null)
        locationClient?.stopLocation()
        val newLocationClientSingle = AMapLocationClient(SDKUtils.getApplicationContext())
        newLocationClientSingle.setLocationListener(listener)
        // 给定位客户端对象设置定位参数
        newLocationClientSingle.setLocationOption(AMapLocationClientOption().apply {
            // 获取一次定位结果
            isOnceLocation = true
            // 设置为高精度定位模式
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        })
        block.invoke(
            newLocationClientSingle.apply {
                startLocation()
            }
        )
    }

    /**
     * 搜索附近1000米内的地址数据
     */
    inline fun doSearchQueryPoi(
        searchV2: PoiSearchV2?,
        moveLatLonPoint: LatLonPoint?,
        listener: PoiSearchV2.OnPoiSearchListener,
        block: (PoiSearchV2.Query, PoiSearchV2) -> Unit
    ) {
        if (moveLatLonPoint != null) {
            // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
            val poiItemQuery = PoiSearchV2.Query("", "", "").apply {
                cityLimit = true
                pageSize = 20
                pageNum = 0
            }
            val newSearch2: PoiSearchV2
            if (null == searchV2) {
                newSearch2 = PoiSearchV2(SDKUtils.getApplicationContext(), poiItemQuery)
                newSearch2.setOnPoiSearchListener(listener)
            } else {
                newSearch2 = searchV2
            }
            newSearch2.bound = PoiSearchV2.SearchBound(moveLatLonPoint, 1000, true)
            newSearch2.searchPOIAsyn()

            block.invoke(poiItemQuery, newSearch2)
        }
    }

    /**
     * 处理附件地址搜索完的数据
     */
    fun handlePoiSearched(query: PoiSearchV2.Query?,poiResult: PoiResultV2?, resultCode: Int, block: (List<PoiItemV2>) -> Unit) {
        if (resultCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult?.query != null) {
                if (poiResult.query == query) {
                    val poiItems = poiResult.pois
                    if (poiItems != null && poiItems.size > 0) {
                        block.invoke(poiItems)
                        return
                    }
                }
            }
            //无搜索结果
            block.invoke(emptyList())
        }
    }
}