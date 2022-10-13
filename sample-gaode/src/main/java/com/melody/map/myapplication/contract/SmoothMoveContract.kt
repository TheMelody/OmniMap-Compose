package com.melody.map.myapplication.contract

import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState

/**
 * SmoothMoveContract
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/12 14:18
 */
class SmoothMoveContract {
    sealed class Event : IUiEvent {
        object PlayPauseEvent: Event()
    }

    data class State (
        val isStart: Boolean,
        val isMapLoaded: Boolean,
        val trackPoints: List<LatLng>?,
        val bounds: LatLngBounds?,
        val totalDuration: Int,
        val uiSettings: MapUiSettings,
        val bitmapTexture: BitmapDescriptor?,
        val movingTrackMarker: BitmapDescriptor?
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal class Toast(val msg: String): Effect()
    }
}