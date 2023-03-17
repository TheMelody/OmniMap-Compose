package com.melody.bdmap.myapplication.model

import com.baidu.mapapi.model.LatLng

/**
 * BM3DModel
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/17 15:31
 */
data class BM3DDataModel(
    val scale: Float,
    val position: LatLng,
    val modelName: String,
    val modelPath: String
)
