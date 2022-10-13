package com.melody.map.myapplication.contract

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.PoiItemV2
import com.melody.sample.common.state.IUiEffect
import com.melody.sample.common.state.IUiEvent
import com.melody.sample.common.state.IUiState

/**
 * DragDropSelectPointContract
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/10 09:33
 */
class DragDropSelectPointContract {
    sealed class Event : IUiEvent {
        object ShowOpenGPSDialog : Event()
        object HideOpenGPSDialog : Event()
    }

    data class State(
        // 是否点击了强制定位
        val isClickForceStartLocation: Boolean,
        // 是否打开了系统GPS权限
        val isOpenGps: Boolean?,
        // 是否显示打开GPS的确认弹框
        val isShowOpenGPSDialog: Boolean,
        // 当前用户自身定位所在的位置
        val currentLocation: LatLng?,
        // 当前手持设备的方向
        val currentRotation: Float,
        // poi列表
        val poiItems: List<PoiItemV2>?,
    ) : IUiState

    sealed class Effect : IUiEffect {
        internal data class Toast(val msg: String?) : Effect()
    }
}