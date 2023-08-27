package com.melody.petal.map_sample.repo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huawei.hms.maps.model.Dash
import com.huawei.hms.maps.model.Gap
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.PatternItem
import com.melody.map.petal_compose.overlay.PolylineRainbow


/**
 * OverlayRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/08/27 16:02
 */
object OverlayRepository {
    fun initPolygonHolePointList(): List<LatLng>{
        return listOf(
            LatLng(39.984864, 116.305756),
            LatLng(39.983618, 116.305848),
            LatLng(39.982347, 116.305966),
            LatLng(39.982412, 116.308111),
            LatLng(39.984122, 116.308224),
            LatLng(39.984955, 116.308099),
            LatLng(39.984864, 116.305756)
        )
    }

    fun initPolygonPatterns(): List<PatternItem> {
        val list = mutableListOf<PatternItem>()
        val points = initPolygonHolePointList()
        points.forEach { _ ->
            list.add(Dash(10F))
            list.add(Gap(5F))
        }
        return list
    }


    fun initPolylineRainbow(): PolylineRainbow {
        return PolylineRainbow.create(
            colors = listOf(
                Color.Red.toArgb(),
                Color.Red.toArgb(),
                Color.Red.toArgb(),
                Color.Blue.toArgb(),
                Color.Blue.toArgb(),
                Color.Blue.toArgb(),
                Color.Green.toArgb(),
                Color.Green.toArgb(),
                Color.Yellow.toArgb(),
                Color.Yellow.toArgb(),
                Color.Yellow.toArgb(),
                Color.Yellow.toArgb(),
                Color.Yellow.toArgb(),
                Color.Yellow.toArgb(),
                Color.Magenta.toArgb(),
                Color.Magenta.toArgb(),
                Color.Magenta.toArgb(),
                Color.Magenta.toArgb(),
                Color.Black.toArgb()
            )
        )
    }

    fun initAnimPolylinePointList(): List<LatLng> {
        return listOf(
            LatLng(39.976748, 116.382314),
            LatLng(39.976851, 116.388045),
            LatLng(39.976892, 116.393597),
            LatLng(39.976906, 116.394199),
            LatLng(39.976906, 116.394298),
            LatLng(39.976996, 116.405949),
            LatLng(39.977016, 116.407692),
            LatLng(39.97701, 116.417564),
            LatLng(39.97701, 116.417564),
            LatLng(39.977127, 116.417591),
            LatLng(39.977127, 116.417582),
            LatLng(39.969017, 116.417932),
            LatLng(39.968549, 116.417977),
            LatLng(39.9666, 116.418094),
            LatLng(39.965099, 116.418193),
            LatLng(39.963957, 116.418256),
            LatLng(39.961533, 116.418301),
            LatLng(39.959343, 116.418301),
            LatLng(39.95422, 116.418732),
            LatLng(39.952375, 116.418858),
            LatLng(39.952106, 116.418876),
            LatLng(39.95192, 116.418849),
            LatLng(39.951693, 116.418696),
            LatLng(39.951528, 116.418525),
            LatLng(39.951383, 116.41822),
            LatLng(39.95128, 116.417941),
            LatLng(39.951239, 116.417609),
            LatLng(39.951218, 116.417312),
            LatLng(39.951218, 116.417088),
            LatLng(39.951197, 116.416899),
            LatLng(39.951115, 116.416675),
            LatLng(39.950984, 116.416513),
            LatLng(39.950839, 116.416378),
            LatLng(39.950639, 116.41627),
            LatLng(39.950426, 116.416217),
            LatLng(39.950095, 116.416243),
            LatLng(39.948835, 116.416486),
            LatLng(39.948697, 116.416486),
            LatLng(39.945557, 116.416648),
            LatLng(39.941686, 116.416791),
            LatLng(39.941005, 116.4168),
            LatLng(39.938442, 116.416944),
            LatLng(39.936045, 116.417016),
            LatLng(39.933662, 116.417142),
            LatLng(39.929247, 116.417295),
            LatLng(39.927683, 116.417393),
            LatLng(39.926553, 116.417438),
            LatLng(39.924583, 116.417492),
            LatLng(39.924369, 116.417492),
            LatLng(39.921779, 116.417573),
            LatLng(39.919044, 116.417654),
            LatLng(39.917404, 116.417708),
            LatLng(39.917287, 116.417717),
            LatLng(39.916233, 116.417825),
            LatLng(39.913904, 116.417807)
        )
    }
}