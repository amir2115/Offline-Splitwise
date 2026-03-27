package com.encer.splitwise.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable

@Composable
fun AppAnimatedSection(
    visible: Boolean,
    enter: EnterTransition = appSectionEnter(),
    exit: ExitTransition = appSectionExit(),
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
        content()
    }
}

@Composable
fun AppAnimatedVisibility(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = appSectionEnter(), exit = appSectionExit()) {
        content()
    }
}

fun appSectionEnter(delayMillis: Int = 0): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = 240, delayMillis = delayMillis)) +
        expandVertically(animationSpec = tween(durationMillis = 240, delayMillis = delayMillis))

fun appHeroSectionEnter(delayMillis: Int = 0): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = 280, delayMillis = delayMillis)) +
        slideInVertically(
            animationSpec = tween(durationMillis = 280, delayMillis = delayMillis),
            initialOffsetY = { it / 6 }
        )

fun appSectionExit(): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 180)) +
        shrinkVertically(animationSpec = tween(durationMillis = 180))
