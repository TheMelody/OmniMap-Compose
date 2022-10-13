package com.melody.map.myapplication.contract

import com.amap.api.maps.model.LatLng
import com.melody.map.gd_compose.poperties.MapUiSettings
import com.melody.map.gd_compose.poperties.MapProperties
import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState

/**
 * LocationTrackingContract
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 17:45
 */
class LocationTrackingContract {

    sealed class Event : IUiEvent {
        object ShowOpenGPSDialog : Event()
        object HideOpenGPSDialog : Event()
    }

    data class State(
        // 是否打开了系统GPS权限
        val isOpenGps: Boolean?,
        // 是否显示打开GPS的确认弹框
        val isShowOpenGPSDialog: Boolean,
        // App是否打开了定位权限
        val grantLocationPermission:Boolean,
        // 当前位置的经纬度
        val locationLatLng: LatLng?,
        val mapProperties: MapProperties,
        val mapUiSettings: MapUiSettings
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal data class Toast(val msg: String?) : Effect()
    }
}