package me.salby.podcasts.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

val EmphasizedDecelerate = CubicBezierEasing(.05f, .7f, .1f, 1f)
val EmphasizedAccelerate = CubicBezierEasing(.3f, 0f, .8f, .15f)

fun TopLevelEnterTransition(durationMillis: Int = 200): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis,
            easing = EmphasizedDecelerate
        )
    )

fun TopLevelExitTransition(durationMillis: Int = 200): ExitTransition =
    fadeOut(
        animationSpec = tween(
            durationMillis,
            easing = EmphasizedAccelerate
        )
    )