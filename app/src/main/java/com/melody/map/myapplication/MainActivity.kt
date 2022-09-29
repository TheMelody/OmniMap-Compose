package com.melody.map.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.amap.api.maps.model.*
import com.melody.map.compose.*
import com.melody.map.compose.overlay.*
import com.melody.map.compose.utils.updateMapViewPrivacy

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applicationContext.updateMapViewPrivacy()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                // TODO:临时测试数据,先把高德地图的80%的能力弄出来
                var isMapLoaded by remember { mutableStateOf(false) }
                val singaporeState = rememberMarkerState(position = LatLng(39.903787, 116.426095))
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(39.903787, 116.426095), 11f)
                }
                val circleCenter by remember { mutableStateOf(LatLng(39.903787, 116.426095)) }

                val dragDropAnimatable = remember {
                    Animatable(Size.Zero,Size.VectorConverter)
                }

                LaunchedEffect(cameraPositionState.isMoving) {
                    if (cameraPositionState.isMoving) {
                        dragDropAnimatable.animateTo(Size(45F,20F))
                    } else {
                        dragDropAnimatable.animateTo(Size(25F,11F))
                    }
                }

                LaunchedEffect(cameraPositionState.position) {
                    snapshotFlow {
                        cameraPositionState.position.target
                    }.collect {
                        //Log.d("拖拽选点",">>>>"+it.toString())
                    }
                }

                Box(modifier = Modifier.fillMaxSize()){
                    GDMap(
                        modifier = Modifier.matchParentSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            showMapLogo = false,
                            isZoomGesturesEnabled = true,
                            isScrollGesturesEnabled = true
                        ),
                        onMapLoaded = {
                            isMapLoaded = true
                        }
                    ) {
                        MarkerInfoWindow(icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                            state = rememberMarkerState(position = LatLng(39.93, 116.13)),
                            content = {
                                Card(modifier = Modifier.size(66.dp)) {
                                    Text(text = "哈哈哈")
                                }
                            }
                        )
                        MarkerInfoWindowContent(
                            icon = BitmapDescriptorFactory.fromAsset("red_marker.png"),
                            state = singaporeState,
                            title = "Singapore"
                        ) {
                            Card(modifier = Modifier.size(120.dp)) {
                                Text(it.title ?: "Title", color = Color.Red)
                            }
                        }

                        Circle(
                            center = circleCenter,
                            fillColor = MaterialTheme.colors.secondary,
                            strokeColor = MaterialTheme.colors.secondaryVariant,
                            radius = 1000.0
                        )
                        //Polygon(listOf(LatLng(39.92,116.34),LatLng(39.93,116.34),LatLng(39.92,116.35)))

                        Polyline(points = listOf(LatLng(39.92,116.34),LatLng(39.93,116.34),LatLng(39.92,116.35)))
                        /*val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
                            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                                return URL("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
                            }
                        }
                        TileOverlay(tileProvider = tileProvider)*/
                    }
                    if(isMapLoaded) {
                        MarkerInScreenCenter {
                            dragDropAnimatable.value
                        }
                    }
                }
            }
        }
    }
    @Composable
    private fun BoxScope.MarkerInScreenCenter(dragDropAnimValueProvider: () -> Size) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .drawBehind {
                    drawOval(
                        color = Color.Gray.copy(alpha = 0.7F),
                        topLeft = Offset(
                            size.width / 2 - dragDropAnimValueProvider().width / 2,
                            size.height / 2 - 18F
                        ),
                        size = dragDropAnimValueProvider()
                    )
                }
                .graphicsLayer {
                    translationY = -(dragDropAnimValueProvider().width.coerceAtLeast(5F) / 2)
                },
            painter = painterResource(id = R.drawable.purple_pin),
            contentDescription = null
        )
    }
}