package com.melody.bdmap.myapplication.model

import androidx.compose.ui.graphics.Color
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.BuildingInfo

/**
 * BM3DPrismDataModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 15:31
 */
data class BM3DPrismDataModel(
    val points: List<LatLng>?,
    val buildingInfo: BuildingInfo?,
    val customSideImage: BitmapDescriptor?,
    val sideFaceColor: Color,
    val topFaceColor: Color,
    val enableGrowAnim: Boolean,
    val zIndex: Int,
)
