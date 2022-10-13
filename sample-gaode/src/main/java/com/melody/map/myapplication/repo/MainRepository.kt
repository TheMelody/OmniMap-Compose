package com.melody.map.myapplication.repo

import android.content.Intent
import com.melody.map.myapplication.*
import com.melody.sample.common.utils.SDKUtils
import com.melody.sample.common.utils.StringUtils

/**
 * MainRepository
 * @author TheMelody
 * email developer_melody@163.com
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