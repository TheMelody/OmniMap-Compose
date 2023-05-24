// MIT License
//
// Copyright (c) 2023 被风吹过的夏天
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.melody.map.baidu_compose

import android.os.Bundle
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.baidu.mapapi.map.BaiduMapOptions
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.extensions.awaitMap
import com.melody.map.baidu_compose.model.BDMapComposable
import com.melody.map.baidu_compose.model.MapClickListeners
import com.melody.map.baidu_compose.poperties.DefaultMapProperties
import com.melody.map.baidu_compose.poperties.DefaultMapUiSettings
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.map.baidu_compose.position.CameraPositionState
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import kotlinx.coroutines.awaitCancellation

/**
 * 百度地图 BDMap
 *
 * @param modifier [Modifier]修饰符
 * @param cameraPositionState 地图相机位置状态[CameraPositionState]
 * @param bdMapOptionsFactory 可以传百度地图[BaiduMapOptions]参数，如离线地图开关等。
 * @param properties 地图属性配置[MapProperties]
 * @param uiSettings 地图SDK UI配置[MapUiSettings]
 * @param locationSource 设置定位数据, 只有先允许定位图层后设置数据才会生效，参见: [com.melody.map.baidu_compose.poperties.MapProperties.isMyLocationEnabled]
 * @param onMapLoaded 地图加载完成的回调
 * @param onMapClick 地图单击事件回调，可在这里处理，其他覆盖物不消费拦截的事件
 * @param onMapLongClick 地图长按事件回调
 * @param onMapPOIClick 地图内Poi单击事件回调
 * @param onOnMapTouchEvent 触摸地图的回调
 * @param content 这里面放置-地图覆盖物
 *
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/03/07 17:01
 */
@Composable
fun BDMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    bdMapOptionsFactory: () -> BaiduMapOptions = { BaiduMapOptions() },
    properties: MapProperties = DefaultMapProperties,
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    locationSource: MyLocationData? = null,
    onMapLoaded: () -> Unit = {},
    onMapClick: (LatLng?) -> Unit = {},
    onMapLongClick: (LatLng?) -> Unit = {},
    onMapPOIClick: (MapPoi?) -> Unit = {},
    onOnMapTouchEvent: (MotionEvent?) -> Unit = {},
    content: (@Composable @BDMapComposable () -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, bdMapOptionsFactory())
    }
    AndroidView(modifier = modifier, factory = { mapView })
    MapLifecycle(mapView)
    val mapClickListeners = remember { MapClickListeners() }.also {
        it.onMapLoaded = onMapLoaded
        it.onMapClick = onMapClick
        it.onMapLongClick = onMapLongClick
        it.onMapPOIClick = onMapPOIClick
        it.onOnMapTouchEvent = onOnMapTouchEvent
    }

    val currentLocationSource by rememberUpdatedState(locationSource)
    val currentMapProperties by rememberUpdatedState(properties)
    val currentUiSettings by rememberUpdatedState(uiSettings)
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)

    LaunchedEffect(Unit) {
        disposingComposition {
            mapView.newComposition(parentComposition) {
                MapUpdater(
                    mapUiSettings = currentUiSettings,
                    clickListeners = mapClickListeners,
                    locationSource = currentLocationSource,
                    mapProperties = currentMapProperties,
                    cameraPositionState = cameraPositionState
                )
                currentContent?.invoke()
            }
        }
    }
}

private suspend inline fun disposingComposition(factory: () -> Composition) {
    val composition = factory()
    try {
        awaitCancellation()
    } finally {
        composition.dispose()
    }
}

private suspend inline fun MapView.newComposition(
    parent: CompositionContext,
    noinline content: @Composable () -> Unit
): Composition {
    val map = awaitMap()
    return Composition(
        MapApplier(map, this), parent
    ).apply {
        setContent(content)
    }
}

@Composable
private fun MapLifecycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(previousState)

        lifecycle.addObserver(mapLifecycleObserver)

        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            // fix memory leak
            mapView.onDestroy()
            mapView.removeAllViews()
        }
    }
}

private fun MapView.lifecycleObserver(previousState: MutableState<Lifecycle.Event>): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE ->  {
                // Skip calling mapView.onCreate if the lifecycle did not go through onDestroy - in
                // this case the BDMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(context, Bundle())
                }
            }
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_DESTROY -> {
                // handled in onDispose
            }
            else -> { /* ignore */ }
        }
        previousState.value = event
    }
