package com.melody.map.compose

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
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
import com.melody.map.compose.extensions.awaitMap
import com.melody.map.compose.poperties.DefaultMapProperties
import com.melody.map.compose.poperties.MapProperties
import kotlinx.coroutines.awaitCancellation

/**
 * GDMap
 * @author TheMelody
 * email developer_melody@163.com
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
    content: (@Composable () -> Unit)? = null
) {
    // TODO:下面3个非com.amap.api.maps.UiSettings里面的属性
    //val isMyLocationEnabled: Boolean = false,
    //val isMyLocationButtonEnabled: Boolean = false,
    //val isMapPlaceChoose: Boolean = false
    if (LocalInspectionMode.current) {
        return
    }
    val context = LocalContext.current
    val mapView = remember {
        MapView(context, aMapOptionsFactory()).apply {
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
    val previousState = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    DisposableEffect(context, lifecycle, mapView) {
        val mapLifecycleObserver = mapView.lifecycleObserver(previousState)
        val callbacks = mapView.componentCallbacks()

        lifecycle.addObserver(mapLifecycleObserver)
        context.registerComponentCallbacks(callbacks)

        onDispose {
            lifecycle.removeObserver(mapLifecycleObserver)
            context.unregisterComponentCallbacks(callbacks)
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
                // this case the GoogleMap composable also doesn't leave the composition. So,
                // recreating the map does not restore state properly which must be avoided.
                if (previousState.value != Lifecycle.Event.ON_STOP) {
                    this.onCreate(Bundle())
                }
            }
            Lifecycle.Event.ON_RESUME -> this.onResume()
            Lifecycle.Event.ON_PAUSE -> this.onPause()
            Lifecycle.Event.ON_DESTROY -> {
                //handled in onDispose
            }
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
