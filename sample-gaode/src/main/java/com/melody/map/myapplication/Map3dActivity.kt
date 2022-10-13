package com.melody.map.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.melody.map.gd_compose.GDMap
import com.melody.map.gd_compose.poperties.MapUiSettings

/**
 * Map3dActivity
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 14:09
 */
class Map3dActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                GDMap(
                    modifier = Modifier.matchParentSize(),
                    isTerrainEnable = true,
                    uiSettings = MapUiSettings(
                        isZoomGesturesEnabled = true,
                        isScrollGesturesEnabled = true,
                    )
                )
            }
        }
    }
}