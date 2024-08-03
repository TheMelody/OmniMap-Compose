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

package com.melody.map.petal_compose

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import com.huawei.hms.maps.HuaweiMapOptions
import com.huawei.hms.maps.LocationSource
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MapStyleOptions
import com.huawei.hms.maps.model.PointOfInterest
import com.melody.map.petal_compose.extensions.awaitMap
import com.melody.map.petal_compose.model.HWMapComposable
import com.melody.map.petal_compose.model.MapClickListeners
import com.melody.map.petal_compose.poperties.DefaultMapProperties
import com.melody.map.petal_compose.poperties.DefaultMapUiSettings
import com.melody.map.petal_compose.poperties.MapProperties
import com.melody.map.petal_compose.poperties.MapUiSettings
import com.melody.map.petal_compose.position.CameraPositionState
import com.melody.map.petal_compose.position.rememberCameraPositionState
import kotlinx.coroutines.awaitCancellation
import java.lang.ref.WeakReference

/**
 * 华为花瓣地图Compose
 *
 * @param modifier [Modifier]修饰符
 * @param cameraPositionState 地图相机位置状态[CameraPositionState]
 * @param mapOptionsFactory 可以传华为地图[HuaweiMapOptions]参数，如离线地图开关等。
 * @param mapStyleOptionsFactory 可以传华为地图[MapStyleOptions]参数，用于定义HuaweiMap样式的类,可以自定义地图样式，更改道路、公园和其他兴趣点等功能的视觉显示。除了更改这些特征的样式外，您还可以完全隐藏特征，这意味着您可以控制地图的特定组件。
 *
 * 自定义样式，请点击 [Petal Maps Studio](https://developer.petalmaps.com/console/studio/AddMap)
 *  ```
 *  mapStyleOptionsFactory = {
 *      MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.***.json)
 *  }
 *  ```
 * @param properties 地图属性配置[MapProperties]
 * @param uiSettings 地图SDK UI配置[MapUiSettings]
 * @param locationSource 设置定位数据, 只有先允许定位图层后设置数据才会生效，参见: [com.melody.map.petal_compose.poperties.MapProperties.isMyLocationEnabled]
 * @param onMapLoaded 地图加载完成的回调
 * @param onMapClick 地图单击事件回调，可在这里处理，其他覆盖物不消费拦截的事件
 * @param onMapLongClick 地图长按事件回调
 * @param onMapPOIClick 地图内Poi单击事件回调
 * @param content 这里面放置-地图覆盖物
 *
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2023/8/27 09:42
 */
@Composable
fun HWMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    mapOptionsFactory: () -> HuaweiMapOptions = { HuaweiMapOptions() },
    mapStyleOptionsFactory: () -> MapStyleOptions? = { null },
    properties: MapProperties = DefaultMapProperties,
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    locationSource: LocationSource? = null,
    onMapLoaded: () -> Unit = {},
    onMapClick: (LatLng?) -> Unit = {},
    onMapLongClick: (LatLng?) -> Unit = {},
    onMapPOIClick: (PointOfInterest?) -> Unit = {},
    content: (@Composable @HWMapComposable () -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, mapOptionsFactory())
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
    }

    val currentMapStyleOptions by rememberUpdatedState(mapStyleOptionsFactory())
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
                    cameraPositionState = cameraPositionState,
                    mapStyleOptions = currentMapStyleOptions
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
        MapApplier(map, WeakReference(this)), parent
    ).apply {
        setContent(content)
    }
}

@Composable
private fun MapLifecycle(mapView: MapView) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val previousState = remember { mutableStateOf(androidx.lifecycle.Lifecycle.Event.ON_CREATE) }
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

private fun MapView.lifecycleObserver(previousState: MutableState<androidx.lifecycle.Lifecycle.Event>): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            androidx.lifecycle.Lifecycle.Event.ON_CREATE ->  {
                // Skip calling mapView.onCreate if the lifecycle did not go through onDestroy - in
                // this case the HWMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
            }
            androidx.lifecycle.Lifecycle.Event.ON_START -> this.onStart()
            androidx.lifecycle.Lifecycle.Event.ON_RESUME -> this.onResume()
            androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> this.onPause()
            androidx.lifecycle.Lifecycle.Event.ON_STOP -> this.onStop()
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
