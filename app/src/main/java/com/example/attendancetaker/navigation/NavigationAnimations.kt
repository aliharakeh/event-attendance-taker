package com.example.attendancetaker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * Shared navigation animations for consistent transitions across the app
 */
object NavigationAnimations {

    // Animation duration in milliseconds
    private const val ANIMATION_DURATION = 300

    // Default animation spec for slide animations
    private val slideAnimationSpec = tween<IntOffset>(
        durationMillis = ANIMATION_DURATION,
        easing = EaseInOut
    )

    private val fadeAnimationSpec = tween<Float>(
        durationMillis = ANIMATION_DURATION,
        easing = EaseInOut
    )

    /**
     * Standard enter transition: slide in from right with fade
     */
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = slideAnimationSpec
        ) + fadeIn(animationSpec = fadeAnimationSpec)
    }

    /**
     * Standard exit transition: slide out to left with fade
     */
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = slideAnimationSpec
        ) + fadeOut(animationSpec = fadeAnimationSpec)
    }

    /**
     * Standard pop enter transition: slide in from left with fade (back navigation)
     */
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = slideAnimationSpec
        ) + fadeIn(animationSpec = fadeAnimationSpec)
    }

    /**
     * Standard pop exit transition: slide out to right with fade (back navigation)
     */
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = slideAnimationSpec
        ) + fadeOut(animationSpec = fadeAnimationSpec)
    }
}