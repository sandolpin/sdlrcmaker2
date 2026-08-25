package com.sandolpin.sdlrcmaker2.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

val SdlrcShapes = Shapes(
    small = RoundedCornerShape(8.dp()),
    medium = RoundedCornerShape(14.dp()),
    large = RoundedCornerShape(20.dp()),
)

// androidx.compose.ui.unit.dp の簡易呼び出し用(Shape.kt内でのみ使用)
private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())
