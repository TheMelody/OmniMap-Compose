// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.melody.map.compose.poperties

import com.amap.api.maps.model.MyLocationStyle
import java.util.Objects

internal val DefaultMapProperties = MapProperties()

/**
 * Data class for properties that can be modified on the map.
 *
 * Note: This is intentionally a class and not a data class for binary
 * compatibility on future changes.
 * See: https://jakewharton.com/public-api-challenges-in-kotlin/
 */
class MapProperties(
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val myLocationStyle: MyLocationStyle? = null,
    val maxZoomPreference: Float = 21.0F,
    val minZoomPreference: Float = 0F,
) {

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        isIndoorEnabled == other.isIndoorEnabled &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        myLocationStyle == other.myLocationStyle &&
        maxZoomPreference == other.maxZoomPreference &&
        minZoomPreference == other.minZoomPreference

    override fun hashCode(): Int = Objects.hash(
        isIndoorEnabled,
        isMyLocationEnabled,
        isTrafficEnabled,
        myLocationStyle,
        maxZoomPreference,
        minZoomPreference
    )

    fun copy(
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        myLocationStyle: MyLocationStyle? = this.myLocationStyle,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
    ): MapProperties = MapProperties(
        isIndoorEnabled = isIndoorEnabled,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        myLocationStyle = myLocationStyle,
        maxZoomPreference = maxZoomPreference,
        minZoomPreference = minZoomPreference,
    )

    override fun toString(): String {
        return "MapProperties(isIndoorEnabled=$isIndoorEnabled, " +
                "isMyLocationEnabled=$isMyLocationEnabled, " +
                "isTrafficEnabled=$isTrafficEnabled, " +
                "myLocationStyle=$myLocationStyle, " +
                "maxZoomPreference=$maxZoomPreference, " +
                "minZoomPreference=$minZoomPreference)"
    }
}

