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

package com.melody.map.myapplication.repo

import android.content.Intent
import com.melody.map.myapplication.*
import com.melody.sample.common.utils.SDKUtils
import com.melody.sample.common.utils.StringUtils

/**
 * MainRepository
 * @author 被风吹过的夏天
 * @email developer_melody@163.com
 * @github: https://github.com/TheMelody/OmniMap
 * created 2022/10/09 09:41
 */
object MainRepository {
    fun handleMainFeatureItemClick(item: String) {
        when(item) {
            StringUtils.getString(R.string.gd_map_main_feature_item_basic) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),BasicFeatureActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_overlay) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),OverlayActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_blue_location) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),LocationTrackingActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_smooth_move) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),SmoothMoveActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_drag_drop_select_point) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),DragDropSelectPointActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_3dmap) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),Map3dActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_route_plan) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),RoutePlanActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_multipoint_click) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),MultiPointOverlayActivity::class.java))
            }
            StringUtils.getString(R.string.gd_map_main_feature_item_marker_animation) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(),MarkerAnimationActivity::class.java))
            }
        }
    }

    private fun startActivity(intent: Intent) {
        SDKUtils.getApplicationContext().startActivity(
            intent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}