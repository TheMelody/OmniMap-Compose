package com.melody.bdmap.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.CircleDottedStrokeType
import com.baidu.mapapi.map.CircleHoleOptions
import com.baidu.mapapi.model.LatLng
import com.google.accompanist.flowlayout.FlowRow
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.overlay.Circle
import com.melody.map.baidu_compose.overlay.CircleGradient
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.MarkerInfoWindow
import com.melody.map.baidu_compose.overlay.MarkerInfoWindowContent
import com.melody.map.baidu_compose.overlay.PolylineCustomTexture
import com.melody.map.baidu_compose.overlay.TextOverlay
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.poperties.MapUiSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                BDMap(modifier = Modifier.fillMaxSize(), uiSettings = MapUiSettings(isDoubleClickZoomEnabled = true, isScrollGesturesEnabled = true)){
                    /*//构建折线点坐标
                    val points: MutableList<LatLng> = ArrayList()
                    points.add(LatLng(39.865, 116.444))
                    points.add(LatLng(39.825, 116.494))
                    points.add(LatLng(39.855, 116.534))
                    points.add(LatLng(39.805, 116.594))

                    val textureList: MutableList<BitmapDescriptor> = ArrayList()
                    textureList.add(BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_route_status_red_selected))
                    textureList.add(BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_route_status_green_selected))
                    textureList.add(BitmapDescriptorFactory.fromResource(com.melody.ui.components.R.drawable.ic_map_route_status_yellow_selected))

                    val indexList: MutableList<Int> = ArrayList()
                    indexList.add(0)
                    indexList.add(1)
                    indexList.add(2)
                    PolylineCustomTexture(
                        width = 20,
                        customTexture = PolylineCustomTexture.create(
                            points = points,
                            textureList = textureList,
                            indexList = indexList
                        )
                    )*/

                    /*Marker(icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                        state = rememberMarkerState().apply {
                            position = LatLng(39.963175, 116.400244)
                        })
                    MarkerInfoWindow(
                        icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                        state = rememberMarkerState().apply {
                            position = LatLng(39.906965, 116.401394)
                        },
                        bundle = Bundle().apply {
                        putString("text","我是一个卖报的小画家，嘎嘎香")
                    }, infoWindowYOffset = -60) {
                        Card(modifier = Modifier.requiredSizeIn(maxWidth = 88.dp, minHeight = 66.dp)) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = it.extraInfo.getString("text","")
                            )
                        }
                    }
                    MarkerInfoWindowContent(
                        icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                        state = rememberMarkerState().apply {
                            position = LatLng(39.939723, 116.425541)
                        },
                        bundle = Bundle().apply {
                            putString("title","头戴三叉束发紫金冠体挂西川红棉百花袍身披兽面吞头连环铠腰系勒甲玲珑狮蛮带手持方天画戟坐下嘶风赤兔马之吕小布是也。")
                        }) {
                        FlowRow(modifier = Modifier
                            .width(120.dp)
                            .wrapContentHeight()) {
                            Text(it.extraInfo.getString("title",""), color = Color.Red)
                            Image(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.mipmap.ic_launcher),
                                contentDescription = null
                            )
                        }
                    }*/
                    /*TextOverlay(
                        text = "百度地图SDK",
                        position = LatLng(39.86923, 116.397428),
                        fontSize = 24,
                        fontColor = Color(0xFFF7F7F7),
                        backgroundColor = Color(0xFF00C3E6),
                    )*/
                    
                    /*Circle(
                        radius = 2800,
                        center = LatLng(39.97923, 116.357428),
                        fillColor = Color(0x4DFF0000),
                        strokeColor = Color(0xFFFFC000),
                        holeOptions = CircleHoleOptions().center(LatLng(39.97923, 116.357428)).radius(1800)
                    )*/
                    /*Circle(
                        radius = 2800,
                        center = LatLng(39.97923, 116.357428),
                        isDottedStroke = true,
                        fillColor = Color(0x4DFF0000),
                        strokeColor = Color(0xFF0088FF)
                    )
                    CircleGradient(
                        radius = 2800,
                        center = LatLng(39.833424, 116.377823),
                        centerColor = Color(0x4D5DFAE8),
                        strokeColor = Color(0xC7FFC000),
                        sideColor = Color(0xFF5DE9CC)
                    )*/
                }
            }
        }
    }
}