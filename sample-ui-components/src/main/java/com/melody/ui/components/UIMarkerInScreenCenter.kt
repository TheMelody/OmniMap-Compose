package com.melody.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource

/**
 * 拖拽选点，屏幕中间动画跳动的Icon
 * @author TheMelody
 * email developer_melody@163.com
 * created 2022/10/09 17:35
 */
@Composable
fun BoxScope.UIMarkerInScreenCenter(@DrawableRes resID: Int, dragDropAnimValueProvider: () -> Size) {
    Image(
        modifier = Modifier
            .align(Alignment.Center)
            .drawBehind {
                drawOval(
                    color = Color.Gray.copy(alpha = 0.7F),
                    topLeft = Offset(
                        size.width / 2 - dragDropAnimValueProvider().width / 2,
                        size.height / 2 - 18F
                    ),
                    size = dragDropAnimValueProvider()
                )
            }
            .graphicsLayer {
                translationY = -(dragDropAnimValueProvider().width.coerceAtLeast(5F) / 2)
            },
        painter = painterResource(id = resID),
        contentDescription = null
    )
}