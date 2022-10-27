package com.melody.map.tencent_compose.model

import android.os.Parcelable
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import kotlinx.parcelize.Parcelize

/**
 * 由于腾讯的CameraPosition没有实现Parcelable，我们这里转换一下，才能用rememberSaveable
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/25 15:09
 */
@Parcelize
data class TXCameraPosition(
    val latlng: LatLng,
    val zoom: Float,
    val tilt: Float,
    val bearing: Float
): Parcelable
