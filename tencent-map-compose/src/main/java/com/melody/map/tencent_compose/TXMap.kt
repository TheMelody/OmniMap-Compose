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

package com.melody.map.tencent_compose

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.melody.map.tencent_compose.extensions.awaitMap
import com.melody.map.tencent_compose.model.MapClickListeners
import com.melody.map.tencent_compose.model.TXMapComposable
import com.melody.map.tencent_compose.poperties.DefaultMapProperties
import com.melody.map.tencent_compose.poperties.DefaultMapUiSettings
import com.melody.map.tencent_compose.poperties.MapProperties
import com.melody.map.tencent_compose.poperties.MapUiSettings
import com.melody.map.tencent_compose.position.CameraPositionState
import com.melody.map.tencent_compose.position.rememberCameraPositionState
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.MapView
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import kotlinx.coroutines.awaitCancellation

/**
 * 腾讯地图 TXMap
 *
 * @param modifier [Modifier]修饰符
 * @param cameraPositionState 地图相机位置状态[CameraPositionState]
 * @param tMapOptionsFactory 可以传腾讯地图[TencentMapOptions]参数，如离线地图开关等。
 * @param properties 地图属性配置[MapProperties]
 * @param uiSettings 地图SDK UI配置[MapUiSettings]
 * @param locationSource 地图定位蓝点功能必传[LocationSource]
 * @param onMapLoaded 地图加载完成的回调
 * @param content 这里面放置-地图覆盖物
 *
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/8/26 15:38
 */
@Composable
fun TXMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    tMapOptionsFactory: () -> TencentMapOptions = { TencentMapOptions() },
    properties: MapProperties = DefaultMapProperties,
    uiSettings: MapUiSettings = DefaultMapUiSettings,
    locationSource: LocationSource? = null,
    onMapLoaded: () -> Unit = {},
    content: (@Composable @TXMapComposable () -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, tMapOptionsFactory()).apply {
            id = R.id.map
        }
    }
    AndroidView(modifier = modifier, factory = { mapView })
    MapLifecycle(mapView)
    val mapClickListeners = remember { MapClickListeners() }.also {
        it.onMapLoaded = onMapLoaded
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
    //val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(/*previousState*/)
        lifecycle.addObserver(mapLifecycleObserver)

        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            // fix memory leak
            mapView.onDestroy()
            mapView.removeAllViews()
        }
    }
}

private fun MapView.lifecycleObserver(/*previousState: MutableState<Lifecycle.Event>*/): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> this.onStart()
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_STOP -> this.onStop()
            Lifecycle.Event.ON_DESTROY -> {
                // handled in onDispose
            }
            else -> { /* ignore */ }
        }
        //previousState.value = event
    }
