package com.melody.bdmap.myapplication.repo

import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.building.BuildingSearch
import com.baidu.mapapi.search.building.BuildingSearchOption
import com.baidu.mapapi.search.core.SearchResult
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
            isZoomEnabled = true
        )
    }
    fun initMapProperties(): MapProperties {
        return MapProperties(isShowBuildings = false)
    }

    suspend fun searchBuilding(latLng: LatLng): List<BM3DPrismDataModel> {
        return suspendCancellableCoroutine { coroutine->
            val buildingSearch = BuildingSearch.newInstance()
            buildingSearch.setOnGetBuildingSearchResultListener { result->
                if (null == result || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    coroutine.resumeWith(Result.failure(NullPointerException()))
                } else {
                    val list: MutableList<BM3DPrismDataModel> = mutableListOf()
                    result.buildingList.forEach{ buildingInfo ->
                        list.add(
                            BM3DPrismDataModel(
                                buildingInfo = buildingInfo,
                                sideFaceColor = Color(0xAAFF0000),
                                topFaceColor = Color(0xAA00FF00),
                                enableGrowAnim = true,
                                zIndex =17,
                                points = null,
                                customSideImage = null
                            )
                        )
                    }
                    coroutine.resumeWith(Result.success(list))
                }
            }
            val buildingSearchOption = BuildingSearchOption()
            buildingSearchOption.latLng = latLng
            buildingSearch.requestBuilding(buildingSearchOption)
        }
    }


}