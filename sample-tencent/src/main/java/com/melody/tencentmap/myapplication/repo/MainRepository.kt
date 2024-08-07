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

package com.melody.tencentmap.myapplication.repo

import android.content.Intent
import com.melody.sample.common.utils.SDKUtils
import com.melody.sample.common.utils.StringUtils
import com.melody.tencentmap.myapplication.BasicFeatureActivity
import com.melody.tencentmap.myapplication.DragDropSelectPointActivity
import com.melody.tencentmap.myapplication.LocationTrackingActivity
import com.melody.tencentmap.myapplication.LogisticsActivity
import com.melody.tencentmap.myapplication.MarkerAnimationActivity
import com.melody.tencentmap.myapplication.MarkerClusterActivity
import com.melody.tencentmap.myapplication.MovementTrackActivity
import com.melody.tencentmap.myapplication.MovementTrackActivity2
import com.melody.tencentmap.myapplication.OverlayActivity
import com.melody.tencentmap.myapplication.R
import com.melody.tencentmap.myapplication.RoutePlanActivity
import com.melody.tencentmap.myapplication.SmoothMoveActivity

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
            StringUtils.getString(R.string.tx_map_main_feature_item_basic) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), BasicFeatureActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_overlay) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), OverlayActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_blue_location) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), LocationTrackingActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_smooth_move) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), SmoothMoveActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_drag_drop_select_point) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), DragDropSelectPointActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_route_plan) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), RoutePlanActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_logistics) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), LogisticsActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_marker_animation) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), MarkerAnimationActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_movement_track) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), MovementTrackActivity::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_movement_track2) -> {
                // 其实 OverlayActivity 这里面已经有移动的示例了，再拉一个页面写，防止有些同学又搞晕了
                // 这里用了腾讯地图的线段动画的能力，包括后面的【路径规划示例】，不会很死板，我们加了线段动画
                startActivity(Intent(SDKUtils.getApplicationContext(), MovementTrackActivity2::class.java))
            }
            StringUtils.getString(R.string.tx_map_main_feature_item_cluster_effect) -> {
                startActivity(Intent(SDKUtils.getApplicationContext(), MarkerClusterActivity::class.java))
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