package com.melody.map.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class MapClickListeners {
    var onMapLoaded: () -> Unit by mutableStateOf({})
    //var onMyLocationButtonClick: () -> Boolean by mutableStateOf({ false })
    //var onMyLocationClick: (Location) -> Unit by mutableStateOf({})
}
