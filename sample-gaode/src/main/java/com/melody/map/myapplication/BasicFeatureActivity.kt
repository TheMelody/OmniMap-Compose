package com.melody.map.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.melody.map.myapplication.ui.BasicFeatureScreen

/**
 * BasicFeatureActivity
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 14:05
 */
class BasicFeatureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicFeatureScreen()
        }
    }
}