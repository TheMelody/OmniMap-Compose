package com.melody.bdmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.building.BuildingSearch
import com.baidu.mapapi.search.building.BuildingSearchOption
import com.baidu.mapapi.search.core.SearchResult
import com.melody.bdmap.myapplication.model.BM3DBuildDataModel
import com.melody.bdmap.myapplication.model.BM3DPrismDataModel
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * BM3DBuildRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 17:01
 */
object BM3DBuildRepository {

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isScrollGesturesEnabled = true,
            isDoubleClickZoomEnabled = true,
            isZoomEnabled = true
        )
    }
    fun initMapProperties(): MapProperties {
        return MapProperties(isShowBuildings = false)
    }

    suspend fun searchBuilding(latLng: LatLng): List<BM3DBuildDataModel> {
        return suspendCancellableCoroutine { coroutine->
            val buildingSearchOption = BuildingSearchOption()
            buildingSearchOption.latLng = latLng
            val buildingSearch = BuildingSearch.newInstance()
            buildingSearch.setOnGetBuildingSearchResultListener { result->
                if (null == result || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    val resultMsg = result?.error?.let { "错误码:" + it.ordinal + ",错误描述:" + it.name } ?: ""
                    coroutine.resumeWith(Result.failure(Exception(resultMsg)))
                } else {
                    val list: MutableList<BM3DBuildDataModel> = mutableListOf()
                    result.buildingList.forEach{ buildingInfo ->
                        list.add(
                            BM3DBuildDataModel(
                                buildingInfo = buildingInfo,
                                sideFaceColor = Color(0xAAFF0000),
                                topFaceColor = Color(0xAA00FF00),
                                customSideImage = null,
                                zIndex = 16,
                                enableAnim = true
                            )
                        )
                    }
                    coroutine.resumeWith(Result.success(list))
                }
            }
            buildingSearch.requestBuilding(buildingSearchOption)
        }
    }


}