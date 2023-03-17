package com.melody.map.baidu_compose.model

import android.os.Parcelable
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.model.LatLng
import kotlinx.parcelize.Parcelize

/**
 * 统一配置百度地图的MapStatus数据，用于百度地图移动相机数据配置
 * @param latLng    地图操作的中心点
 * @param zoom     地图缩放级别 4~21，室内图支持到22
 * @param rotate   地图旋转角度
 * @param overlook 地图俯仰角度： -45~0
 *
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/08 09:41
 */
@Parcelize
data class BDCameraPosition(
    val latLng: LatLng,
    val zoom: Float,
    val rotate: Float,
    val overlook: Float
): Parcelable {

    companion object {
        internal fun convertMapStatusData(mapStatus: MapStatus): BDCameraPosition {
            return BDCameraPosition(
                latLng = mapStatus.target,
                zoom = mapStatus.zoom,
                rotate = mapStatus.rotate,
                overlook = mapStatus.overlook
            )
        }

        internal fun toMapStatus(position: BDCameraPosition): MapStatus {
            return MapStatus.Builder()
                .target(position.latLng).zoom(position.zoom)
                .rotate(position.rotate).overlook(position.overlook)
                .build()
        }
    }
}
