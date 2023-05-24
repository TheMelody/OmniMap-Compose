package com.melody.bdmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.district.DistrictResult
import com.baidu.mapapi.search.district.DistrictSearch
import com.baidu.mapapi.search.district.DistrictSearchOption
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener
import com.melody.bdmap.myapplication.model.BM3DPrismDataModel
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * BM3DPrismRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 17:01
 */
object BM3DPrismRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isDoubleClickZoomEnabled = true,
            isTiltGesturesEnabled = true,
            isRotateGesturesEnabled = true,
            isFlingEnable = true,
            isZoomEnabled = true
        )
    }
    fun initMapProperties(): MapProperties {
        return MapProperties(isShowBuildings = false)
    }

    fun init3DPrismData(points: List<List<LatLng>> ): BM3DPrismDataModel {
        return BM3DPrismDataModel(
            sideFaceColor = Color(0xAAFF0000),
            topFaceColor = Color(0xAA00FF00),
            points = points,
            customSideImage = null
        )
    }

    suspend fun queryDistrictData():List<List<LatLng>> {
        return suspendCancellableCoroutine { continuation->
            val search = DistrictSearch.newInstance()
            search.setOnDistrictSearchListener(object: OnGetDistricSearchResultListener{
                override fun onGetDistrictResult(p0: DistrictResult?) {
                    if (null != p0 && p0.error == SearchResult.ERRORNO.NO_ERROR) {
                        if (p0.polylines == null) {
                            continuation.resumeWith(Result.failure(Exception("抱歉，没有获取到结果")))
                            return
                        }
                        continuation.resumeWith(Result.success(p0.polylines))
                    } else {
                        continuation.resumeWith(Result.failure(Exception("抱歉，没有获取到结果")))
                    }
                }
            })
            search.searchDistrict(DistrictSearchOption().cityName("北京市").districtName("海淀区"))
        }
    }
}