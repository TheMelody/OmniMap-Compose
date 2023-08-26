// MIT License
//
// Copyright (c) 2022 被风吹过的夏天
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

package com.melody.map.gd_compose

import android.content.ComponentCallbacks
import android.content.res.Configuration
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
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.melody.map.gd_compose.extensions.awaitMap
import com.melody.map.gd_compose.model.GDMapComposable
import com.melody.map.gd_compose.model.MapClickListeners
import com.melody.map.gd_compose.poperties.DefaultMapProperties
import com.melody.map.gd_compose.poperties.DefaultMapUiSettings
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.position.CameraPositionState
import com.melody.map.gd_compose.position.rememberCameraPositionState
import kotlinx.coroutines.awaitCancellation

/**
 * 高德地图 GDMap
 *
 * @param modifier [Modifier]修饰符
 * @param cameraPositionState 地图相机位置状态[CameraPositionState]
 * @param aMapOptionsFactory 可以传高德地图[AMapOptions]参数，如离线地图开关等。
 * @param properties 地图属性配置[MapProperties]
 * @param uiSettings 地图SDK UI配置[MapUiSettings]
 * @param locationSource 设置定位数据, 只有先允许定位图层后设置数据才会生效，参见: [com.melody.map.gd_compose.poperties.MapProperties.isMyLocationEnabled]
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
 * created 2022/8/26 15:38
 */
@Composable
fun GDMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    aMapOptionsFactory: () -> AMapOptions = { AMapOptions() },
    properties: MapProperties = DefaultMapProperties,
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    locationSource: LocationSource? = null,
    onMapLoaded: () -> Unit = {},
    onMapClick: (LatLng?) -> Unit = {},
    onMapLongClick: (LatLng?) -> Unit = {},
    onMapPOIClick: (Poi?) -> Unit = {},
    onOnMapTouchEvent: (MotionEvent?) -> Unit = {},
    //indoorBuildingActive: (IndoorBuildingInfo?) -> Unit = {},
    content: (@Composable @GDMapComposable () -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, aMapOptionsFactory())
    }
    AndroidView(modifier = modifier, factory = { mapView }, onRelease = {
        it.onDestroy()
        it.removeAllViews()
    })
    MapLifecycle(mapView)
    val mapClickListeners = remember { MapClickListeners() }.also {
        it.onMapLoaded = onMapLoaded
        it.onMapClick = onMapClick
        it.onMapLongClick = onMapLongClick
        it.onMapPOIClick = onMapPOIClick
        it.onOnMapTouchEvent = onOnMapTouchEvent
        //it.indoorBuildingActive = indoorBuildingActive
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
        MapApplier(map, this.context.applicationContext), parent
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
        val callbacks = mapView.componentCallbacks()

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)

        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
        }
    }
    DisposableEffect(mapView) {
        onDispose {
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
                // this case the GDMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
            }
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            else -> { /* ignore */ }
        }
        previousState.value = event
    }

private fun MapView.componentCallbacks(): ComponentCallbacks =
    object : ComponentCallbacks {
        override fun onConfigurationChanged(config: Configuration) {}

        override fun onLowMemory() {
            this@componentCallbacks.onLowMemory()
        }
    }
