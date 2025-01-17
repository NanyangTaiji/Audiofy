/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 19-01-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prime.media.console

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.format.DateUtils.formatElapsedTime
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.IntDef
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Queue
import androidx.compose.material.icons.outlined.ScreenLockLandscape
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.google.accompanist.adaptive.TwoPaneStrategy
import com.google.accompanist.adaptive.VerticalTwoPaneStrategy
import com.google.accompanist.adaptive.calculateDisplayFeatures
import com.prime.media.Material
import com.prime.media.R
import com.prime.media.backgroundColorAtElevation
import com.prime.media.caption2
import com.prime.media.core.Anim
import com.prime.media.core.ContentElevation
import com.prime.media.core.ContentPadding
import com.prime.media.core.MediumDurationMills
import com.prime.media.core.compose.AnimatedIconButton
import com.prime.media.core.compose.LocalNavController
import com.prime.media.core.compose.LocalSystemFacade
import com.prime.media.core.compose.LocalWindowSize
import com.prime.media.core.compose.LottieAnimButton
import com.prime.media.core.compose.LottieAnimation
import com.prime.media.core.compose.PlayerView
import com.prime.media.core.compose.Reach
import com.prime.media.core.compose.WindowSize
import com.prime.media.core.compose.marque
import com.prime.media.core.compose.menu.DropdownMenu2
import com.prime.media.core.compose.menu.DropdownMenuItem2
import com.prime.media.core.compose.modifiers.ImageBrush
import com.prime.media.core.compose.modifiers.visualEffect
import com.prime.media.core.compose.preference
import com.prime.media.core.playback.artworkUri
import com.prime.media.core.playback.subtitle
import com.prime.media.core.playback.title
import com.prime.media.core.util.DateUtils
import com.prime.media.darkShadowColor
import com.prime.media.effects.AudioFx
import com.prime.media.isAppearanceLightSystemBars
import com.prime.media.lightShadowColor
import com.prime.media.outline
import com.prime.media.settings.Settings
import com.prime.media.small2
import com.primex.core.SignalWhite
import com.primex.core.activity
import com.primex.core.plus
import com.primex.core.withSpanStyle
import com.primex.material2.IconButton
import com.primex.material2.Label
import com.primex.material2.OutlinedButton2
import com.primex.material2.neumorphic.NeumorphicButton
import com.primex.material2.neumorphic.NeumorphicButtonDefaults
import ir.mahozad.multiplatform.wavyslider.material.WavySlider

private const val TAG = "ConsoleView"

/** Default background style. */
private const val DEFAULT_BACKGROUND = 0

/** Background style featuring artwork. */
private const val BACKGROUND_ARTWORK = 1

/** Background style with a gradient. */
private const val BACKGROUND_GRADIENT = 2

/** Background style black color when video*/
private const val BACKGROUND_VIDEO_SURFACE = 3

/**
 * Annotation to indicate valid values for Background styles.
 *
 * Supported values:
 *  - [DEFAULT_BACKGROUND]
 *  - [BACKGROUND_ARTWORK]
 *  - [BACKGROUND_GRADIENT]
 */
@IntDef(DEFAULT_BACKGROUND, BACKGROUND_ARTWORK, BACKGROUND_GRADIENT, BACKGROUND_VIDEO_SURFACE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
private annotation class Background

/** Simple seekbar style. */
private const val SEEKBAR_STYLE_SIMPLE = 0

/** Wavy seekbar style. */
private const val SEEKBAR_STYLE_WAVY = 1

/**
 * Annotation to indicate valid values for Seekbar styles.
 *
 * Supported values:
 *  - [SEEKBAR_STYLE_SIMPLE]
 *  - [SEEKBAR_STYLE_WAVY]
 */
@IntDef(SEEKBAR_STYLE_SIMPLE, SEEKBAR_STYLE_WAVY)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
private annotation class Seekbar

/** Simple play button style. */
private const val PLAY_BUTTON_STYLE_SIMPLE = 0

/** Neumorphic play button style. */
private const val PLAY_BUTTON_STYLE_NEUMORPHIC = 1

/**
 * Annotation to restrict valid values for play button styles.
 *
 * Supported values:
 *  - [PLAY_BUTTON_STYLE_SIMPLE]
 *  - [PLAY_BUTTON_STYLE_NEUMORPHIC]
 */
@IntDef(PLAY_BUTTON_STYLE_SIMPLE, PLAY_BUTTON_STYLE_NEUMORPHIC)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
private annotation class PlayButton

/**
 * Constants representing different types of requests that a component can make to its parent.
 */
private const val REQUEST_SHOW_PLAYING_QUEUE = 1

/** Request to show the properties dialog. */
private const val REQUEST_SHOW_PROPERTIES = 2

/** Request to handle a back press event. */
private const val REQUEST_HANDLE_BACK_PRESS = 3


/** Request to show light */
private const val REQUEST_REQUIRES_LIGHT_SYSTEM_BARS = 4

/** @see */
private const val REQUEST_REQUIRES_DARK_SYSTEM_BARS = 5

/**
 * Request to toggle the [Console] visibility between [Console.VISIBILITY_LOCKED] and [Console.VISIBILITY_VISIBLE]
 */
private const val REQUEST_TOOGLE_LOCK = 6

/**
 * @see REQUEST_TOOGLE_LOCK
 */
private const val REQUEST_TOGGLE_VISIBILITY = 7


/**
 * Toggles [toggleRotationLock] between locked and unlocked.
 */
private const val REQUEST_TOGGLE_ROTATION_LOCK = 8

/**
 * Annotation to restrict valid values for request types.
 *
 * This annotation also serves as a signal to the parent component that handles
 * different actions based on the request type:
 *  - Showing/hiding dialogs (toggling visibility if the same request is passed again)
 *  - Handling back button presses
 *
 * Supported values:
 *  - [REQUEST_HANDLE_BACK_PRESS]
 *  - [REQUEST_SHOW_PLAYING_QUEUE]
 *  - [REQUEST_SHOW_PROPERTIES]
 *  - [REQUEST_REQUIRES_LIGHT_SYSTEM_BARS]
 *  - [REQUEST_REQUIRES_DARK_SYSTEM_BARS]
 */
@IntDef(
    REQUEST_HANDLE_BACK_PRESS,
    REQUEST_SHOW_PLAYING_QUEUE,
    REQUEST_SHOW_PROPERTIES,
    REQUEST_REQUIRES_LIGHT_SYSTEM_BARS,
    REQUEST_REQUIRES_DARK_SYSTEM_BARS,
    REQUEST_TOOGLE_LOCK,
    REQUEST_TOGGLE_VISIBILITY,
    REQUEST_TOGGLE_ROTATION_LOCK
)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
private annotation class Request

/**
 * Extension property for [WindowInsets] that provides a [DpRect] representation of the insets,
 * ensuring layout compatibility across different screen densities and layout directions.
 *
 * @return A [DpRect] containing the left, top, right, and bottom insets in density-independent pixels (dp).
 */
private val WindowInsets.asDpRect: DpRect
    @Composable
    @ReadOnlyComposable
    get() {
        val ld =
            LocalLayoutDirection.current  // Get current layout direction for correct inset handling
        val density = LocalDensity.current    // Get current screen density for conversion to dp
        with(density) {
            // Convert raw insets to dp values, considering layout direction
            return DpRect(
                left = getLeft(density, ld).toDp(),
                right = getRight(this, ld).toDp(),
                top = getTop(this).toDp(),
                bottom = getBottom(this).toDp()
            )
        }
    }

/**
 * Checks if this [DpSize] is large enough to contain another [DpSize].
 *
 * This extension function is useful for determining whether a given size can
 * accommodate another size within its bounds. For example, it can be used to
 * check if a layout can fit a child view or if an image can be displayed
 * within a certain area.
 *
 * @param other The other [DpSize] to check against.
 * @return `true` if this [DpSize] is equal to or larger than the other [DpSize]
 *         in both width and height, meaning it can fully contain the other size.
 *         Returns `false` otherwise.
 */
fun DpSize.contains(other: DpSize): Boolean {
    // Ensure that both the width and height of this DpSize are greater than
    // or equal to the corresponding dimensions of the other DpSize to indicate
    // that it can fully contain the other size within its bounds.
    return this.width >= other.width && this.height >= other.height
}

/**
 * Extensions for the [Console] class, providing convenient access to media metadata.
 */
private inline val Console.title: String?
    get() = current?.title?.toString()

/**
 * Retrieves the subtitle of the currently playing media item, if available.
 */
private inline val Console.subtitle: String?
    get() = current?.subtitle?.toString()

/**
 * Retrieves the artwork URI of the currently playing media item, providing
 * convenient access for UI elements and data management.
 */
private inline val Console.artworkUri: Uri?
    get() = current?.artworkUri

/**
 * Utility Fun that toggles [Console.resizeMode]
 */
@Deprecated("just remove this and replace with onsite cycling.")
private fun Console.cycleResizeMode() {
    resizeMode =
        if (resizeMode == Console.RESIZE_MODE_FILL) Console.RESIZE_MORE_FIT else Console.RESIZE_MODE_FILL
}

/**
 * Shows or hides the system bars, such as the status bar and the navigation bar.
 * @param enable A boolean value that indicates whether to show or hide the system bars.
 * If true, the system bars are hidden. If false, the system bars are shown.
 */
// Explore implications of toggling immersive mode when the user has hidden system bars.
// TODO: Assess potential outcomes and address any issues that may arise.
private fun WindowInsetsControllerCompat.immersiveMode(enable: Boolean) =
    if (enable) hide(WindowInsetsCompat.Type.systemBars()) else show(WindowInsetsCompat.Type.systemBars())

/**
 * Toggles the screen rotation lock for the current activity.
 *
 * If the current screen orientation is unspecified ([ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED]),
 * this function sets the requested screen orientation to [ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE],
 * effectively locking the screen to landscape mode. Otherwise, it resets the screen orientation to
 * [ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED], allowing the system to determine the orientation based
 * on the device sensor.
 *
 * @return `true` if the screen orientation is locked to landscape after the toggle,
 *         `false` otherwise.
 *
 * @see ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
 * @see ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
 */
private fun Activity.toggleRotationLock(): Boolean {
    // Determine the new screen orientation based on the current state.
    val rotation =
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    // Set the requested screen orientation to the calculated value.
    requestedOrientation = rotation

    // Return the weather orientation is locked or not.
    return rotation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

/**
 * Returns `true` if the screen orientation is locked, `false` otherwise.
 *
 * @see ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
 */
private inline val Activity.isOrientationLocked
    get() = requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

/**
 * Ensures that the console is always visible. If the current visibility is set to
 * [Console.VISIBILITY_LOCKED] the visibility remains unchanged.
 *
 * Sets the console visibility to [Console.VISIBILITY_ALWAYS] if the given parameter is true and the current visibility is not [Console.VISIBILITY_LOCKED] or [Console.VISIBILITY_ALWAYS].
 * If the given parameter is false and the current visibility is [Console.VISIBILITY_ALWAYS], it sets the visibility to [Console.VISIBILITY_VISIBLE].
 * This function has no effect if the console is a video console, as it is always visible in that case.
 * @param enabled A boolean value indicating whether to ensure the console is always visible or not.
 */
private fun Console.ensureAlwaysVisible(enabled: Boolean) {
    visibility = when {
        visibility == Console.VISIBILITY_LOCKED -> return
        !isVideo -> return // because in this case it will always be visible.
        visibility == Console.VISIBILITY_ALWAYS && !enabled -> Console.VISIBILITY_VISIBLE
        else -> Console.VISIBILITY_ALWAYS
    }
}

// Different shapes
private val Rounded_15 = RoundedCornerShape(15)
private val DefaultArtworkShape = Rounded_15

/**
 * Composable function representing a SeekBar.
 *
 * @param value The current value of the SeekBar between 0 and 1, [Float.NaN] indicates waiting.
 * @param onValueChange Callback triggered when the SeekBar value changes.
 * @param modifier Optional Modifier for additional styling.
 * @param color The color of the SeekBar, default is the primary color from MaterialTheme.
 * @param style The style of the SeekBar, either [SEEKBAR_STYLE_SIMPLE] or [SEEKBAR_STYLE_WAVY].
 */
@Composable
@NonRestartableComposable
private fun SeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = Material.colors.primary,
    @Seekbar style: Int = SEEKBAR_STYLE_SIMPLE,
) {
    // if the value is Float.NaN; show a non ending progress bar.
    when (value.isNaN()) {
        true -> LinearProgressIndicator(
            modifier = modifier,
            color = accent,
            strokeCap = StrokeCap.Round,
        )

        else -> WavySlider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            // idp because 0 dp is not supported.
            waveLength = if (style == SEEKBAR_STYLE_SIMPLE) 0.dp else 20.dp,
            waveHeight = if (style == SEEKBAR_STYLE_SIMPLE) 0.dp else 7.dp,
            incremental = true,
            colors = SliderDefaults.colors(activeTrackColor = accent, thumbColor = accent)
        )
    }
}

private val RoundedCornerShape_24 = RoundedCornerShape(24)

/**
 * Composable function representing a PlayButton with different styles.
 *
 * @param onClick Callback triggered when the PlayButton is clicked.
 * @param isPlaying Whether the media is currently playing.
 * @param modifier Optional Modifier for additional styling.
 * @param style The style of the PlayButton, either [PLAY_BUTTON_STYLE_SIMPLE], [PLAY_BUTTON_STYLE_NEUMORPHIC],
 * or [PLAY_BUTTON_STYLE_FAB].
 */
// TODO: Ensure each play button has a distinct style of surface only.
@Composable
@NonRestartableComposable
private fun PlayButton(
    onClick: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    @PlayButton style: Int = PLAY_BUTTON_STYLE_NEUMORPHIC,
) {
    when (style) {
        // Use the simple version of the play button.
        // The tint in this case is derived from the LocalContentColor.
        PLAY_BUTTON_STYLE_SIMPLE ->
            IconButton(
                painter = painterResource(id = if (isPlaying) R.drawable.media3_notification_pause else R.drawable.media3_notification_play),
                modifier = modifier.scale(1.5f),
                onClick = onClick
            )

        PLAY_BUTTON_STYLE_NEUMORPHIC -> NeumorphicButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape_24,
            colors = NeumorphicButtonDefaults.neumorphicButtonColors(
                lightShadowColor = Material.colors.lightShadowColor,
                darkShadowColor = Material.colors.darkShadowColor
            ),
            border = if (!Material.colors.isLight)
                BorderStroke(1.dp, Material.colors.outline.copy(0.06f))
            else null,
            content = {
                LottieAnimation(
                    id = R.raw.lt_play_pause,
                    atEnd = !isPlaying,
                    scale = 1.5f,
                    progressRange = 0.0f..0.29f,
                    duration = Anim.MediumDurationMills,
                    easing = LinearEasing
                )
            }
        )
        // handle others
        else -> TODO("$style Not Implemented Yet!")
    }
}

/**
 * No-op pointer input [Modifier] that discards pointer events.
 */
private val NoOpPointerInput = Modifier.pointerInput(Unit) {}

/**
 * Defines a row of controls, consisting of 5 buttons with a play button at the center.
 * The color of icons is determined by [LocalContentColor].
 *
 * @param state The current state of the console.
 * @param style The style of the play button, default is [PLAY_BUTTON_STYLE_NEUMORPHIC].
 */
@Composable
private inline fun Controls(
    state: Console,
    modifier: Modifier = Modifier,
    @PlayButton style: Int = PLAY_BUTTON_STYLE_NEUMORPHIC
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.then(NoOpPointerInput)
) {
    // Shuffle | Option 5
    val shuffle = state.shuffle
    val facade = LocalSystemFacade.current
    LottieAnimButton(
        id = R.raw.lt_shuffle_on_off,
        onClick = { state.toggleShuffle(); facade.launchReviewFlow(); },
        atEnd = !shuffle,
        progressRange = 0f..0.8f,
        scale = 1.5f
    )

    var enabled = !state.isFirst
    val onColor = LocalContentColor.current
    IconButton(
        onClick = { state.skipToPrev(); facade.launchReviewFlow() },
        painter = rememberVectorPainter(image = Icons.Outlined.KeyboardDoubleArrowLeft),
        contentDescription = null,
        enabled = enabled,
        tint = onColor.copy(if (enabled) ContentAlpha.high else ContentAlpha.medium)
    )

    // play_button
    PlayButton(
        onClick = { state.togglePlay(); facade.launchReviewFlow() },
        isPlaying = state.isPlaying,
        modifier = Modifier
            .padding(horizontal = ContentPadding.medium)
            .size(60.dp),
        style = style
    )

    // Skip to Next
    enabled = !state.isLast
    IconButton(
        onClick = { state.skipToNext(); facade.launchReviewFlow() },
        painter = rememberVectorPainter(image = Icons.Outlined.KeyboardDoubleArrowRight),
        contentDescription = null,
        enabled = enabled,
        tint = onColor.copy(if (enabled) ContentAlpha.high else ContentAlpha.medium)
    )

    // CycleRepeatMode | Option 6
    val mode = state.repeatMode
    AnimatedIconButton(
        id = R.drawable.avd_repeat_more_one_all,
        onClick = { state.cycleRepeatMode(); facade.launchReviewFlow(); },
        atEnd = mode == Player.REPEAT_MODE_ALL,
        tint = onColor.copy(if (mode == Player.REPEAT_MODE_OFF) ContentAlpha.disabled else ContentAlpha.high)
    )
}

/**
 * Composable function displaying a progress bar along with two icon buttons.
 *
 * @param style The style of the progress bar, default is [SEEKBAR_STYLE_WAVY].
 * @param accent The accent color for the progress bar, default is the primary color from MaterialTheme.
 * @param state The current state of the console.
 */
@Composable
private fun TimeBar(
    state: Console,
    modifier: Modifier = Modifier,
    onRequest: (request: @Request Int) -> Boolean,
    @Seekbar style: Int = SEEKBAR_STYLE_WAVY,
    accent: Color = Material.colors.primary,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.then(NoOpPointerInput)
) {
    // resize_mode
    val resizeMode = state.resizeMode
    IconButton(
        imageVector = if (resizeMode == Console.RESIZE_MODE_FILL) Icons.Outlined.Fullscreen else Icons.Outlined.FitScreen,
        onClick = state::cycleResizeMode,
        enabled = state.isVideo
    )

    SeekBar(
        value = if (state.isSeekable) state.progress else Float.NaN,
        style = style,
        onValueChange = { state.progress = it },
        accent = accent,
        modifier = Modifier.weight(1f)
    )

    val context = LocalContext.current
    IconButton(
        imageVector = when {
            LocalInspectionMode.current || context.activity.isOrientationLocked -> Icons.Outlined.ScreenLockLandscape
            else -> Icons.Outlined.ScreenRotation
        },
        onClick = { onRequest(REQUEST_TOGGLE_ROTATION_LOCK) }
    )
}

/**
 * A composable function for creating a menu item with optional title, subtitle, icon, and click action.
 *
 * @param title The main text content of the menu item.
 * @param onClick The callback to be invoked when the menu item is clicked.
 * @param modifier Optional [Modifier] to apply to the menu item.
 * @param subtitle Optional secondary text content of the menu item.
 * @param icon Optional [ImageVector] icon to be displayed alongside the menu item.
 * @param enabled Whether the menu item is interactive and can be clicked.
 *
 * @sample MenuItem(
 *     title = "Settings",
 *     onClick = { /* Handle click action */ },
 *     modifier = Modifier.padding(8.dp),
 *     subtitle = "Configure app preferences",
 *     icon = Icons.Default.Settings,
 *     enabled = true
 * )
 *
 * @see Composable
 */
//TODO: Maybe make parent only accept subtitle.
@Composable
private inline fun MenuItem(
    title: CharSequence,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: CharSequence? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    DropdownMenuItem2(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leading = if (icon == null) null else rememberVectorPainter(image = icon),
        title = buildAnnotatedString {
            append(title)
            if (subtitle == null) return@buildAnnotatedString
            withSpanStyle(
                color = LocalContentColor.current.copy(ContentAlpha.disabled),
                fontSize = 11.sp,
                block = {
                    append("\n" + subtitle)
                }
            )
        },
    )
}

/**
 * Composable function representing additional options available in a popup menu.
 * The 'More' menu includes the icon and the necessary logic for displaying different menus.
 *
 * @param state The current state of the console.
 * @param modifier Optional Modifier for additional styling.
 */
@Composable
private fun More(
    state: Console,
    onRequest: (request: @Request Int) -> Boolean,
    modifier: Modifier = Modifier
) {
    // If the 'More' is compact, it includes options as well.
    // Represents the state of all menus in this composable:
    // - 0: Off state.
    // - 1: Main menu
    // - 2: Audio menu
    // - 3: Subtitle menu
    var expanded by remember { mutableIntStateOf(0) }
    // Main Icon
    IconButton(
        onClick = { expanded = 1; state.ensureAlwaysVisible(true) },
        modifier = modifier,
        content = {
            val onColor = LocalContentColor.current
            // The icon of this item.
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = onColor
            )

            // Audio Menu
            DropdownMenu2(
                expanded = expanded == 2,
                onDismissRequest = { expanded = 1 },
                shape = Material.shapes.small2,
                content = {
                    MenuItem(
                        title = "Auto",
                        onClick = { state.currAudioTrack = null; expanded = 1 }
                    )
                    // Others
                    state.audios.forEach { track ->
                        MenuItem(
                            title = track.name,
                            onClick = { state.currAudioTrack = track; expanded = 1 })
                    }
                }
            )

            // Subtitle Menu
            DropdownMenu2(
                expanded = expanded == 3,
                onDismissRequest = { expanded = 1; },
                shape = Material.shapes.small2,

                // TODO - Add one option for enabling/adding custom subtitle track.
                content = {
                    MenuItem(
                        title = "Off",
                        onClick = { state.currSubtitleTrack = null; expanded = 1 }
                    )
                    // Others
                    state.subtiles.forEach { track ->
                        MenuItem(
                            title = track.name,
                            onClick = { state.currSubtitleTrack = track; expanded = 1 })
                    }
                }
            )

            // Main Menu
            DropdownMenu2(
                expanded = expanded == 1,
                onDismissRequest = { expanded = 0; state.ensureAlwaysVisible(false) },
                shape = Material.shapes.small2,
                content = {
                    // A top row; showing common options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            // Equalizer
                            val controller = LocalNavController.current
                            // FixMe: State is not required here. implement to get value without state.
                            val useBuiltIn by preference(key = Settings.USE_IN_BUILT_AUDIO_FX)
                            val facade = LocalSystemFacade.current
                            IconButton(
                                imageVector = Icons.Outlined.Tune,
                                onClick = {
                                    if (useBuiltIn)
                                        controller.navigate(AudioFx.route)
                                    else
                                        facade.launchEqualizer(state.audioSessionId)
                                    expanded = 0
                                }
                            )
                        }
                    )
                    //  A divider between Row and Other items.
                    Divider()
                    // Audio
                    val isVideo = state.isVideo
                    MenuItem(
                        title = "Audio",
                        subtitle = state.currAudioTrack?.name ?: "Auto",
                        onClick = { expanded = 2 },
                        icon = Icons.Outlined.Speaker,
                        enabled = isVideo
                    )

                    // Subtitle
                    MenuItem(
                        title = "Subtitle",
                        subtitle = state.currSubtitleTrack?.name ?: "Off",
                        onClick = { expanded = 3 },
                        icon = Icons.Outlined.ClosedCaption,
                        enabled = isVideo
                    )

                    // Lock
                    MenuItem(
                        title = "Lock",
                        subtitle = "Lock/Hide controller",
                        icon = Icons.Outlined.Lock,
                        enabled = isVideo,
                        onClick = {
                            // handle lock toggle request
                            onRequest(REQUEST_TOOGLE_LOCK)
                            expanded = 0
                        }
                    )

                    // Share
                    // Properties
                }
            )
        }
    )
}

/**
 * Composable function representing a row of options, containing 5 buttons.
 * The tint of icons is derived from [LocalContentColor].
 *
 * @param state The current state of the console.
 */
@Composable
private fun Options(
    state: Console,
    onRequest: (request: @Request Int) -> Boolean,
    modifier: Modifier = Modifier,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.then(NoOpPointerInput)
) {
    // Control the state of components using a single state variable.
    // TODO: Investigate if using a single state for controlling multiple components causes side effects.
    // The value '0' represents all components turned off.
    // The value '1' represents the queue component.
    // The value '2' represents the speed controller component.
    // The value '3' represents the sleep timer component.

    // Represents the current component that is in expanded state.
    var expanded by remember { mutableIntStateOf(0) }
    // show playing queue
    PlayingQueue(
        state = state,
        expanded = expanded == 1,
        // restore the visibility back to normal.
        onDismissRequest = { expanded = 0; state.ensureAlwaysVisible(false) }
    )

    // Speed Controller.
    PlaybackSpeed(
        expanded = expanded == 2,
        value = state.playbackSpeed,
        onValueChange = {
            if (it != -1f)
            // If the value is -1f, it means a dismiss request
                state.playbackSpeed = it
            state.ensureAlwaysVisible(false)
            expanded = 0;
        }
    )

    // Sleep Timer.
    SleepTimer(
        expanded = expanded == 3,
        onValueChange = {
            if (it != -2L)
                state.sleepAfterMills = it
            expanded = 0
            state.ensureAlwaysVisible(false)
        }
    )

    // Queue
    IconButton(
        painter = rememberVectorPainter(image = Icons.Outlined.Queue),
        tint = LocalContentColor.current,
        onClick = {
            // check if parent might handle the request of showing the dialog.
            if (onRequest(REQUEST_SHOW_PLAYING_QUEUE))
                return@IconButton;
            expanded = 1; state.ensureAlwaysVisible(true)
        },
    )

    // Speed Controller.
    IconButton(
        onClick = { expanded = 2; state.ensureAlwaysVisible(true) },
        painter = rememberVectorPainter(image = Icons.Outlined.Speed),
        tint = LocalContentColor.current
    )

    // SleepAfter.
    IconButton(
        onClick = { expanded = 3; state.ensureAlwaysVisible(true) },
        content = {
            val mills = state.sleepAfterMills
            Crossfade(targetState = mills != -1L, label = "SleepAfter CrossFade") { show ->
                when (show) {
                    true -> Label(
                        text = formatElapsedTime(mills / 1000L),
                        style = Material.typography.caption2,
                        fontWeight = FontWeight.Bold,
                        color = Material.colors.secondary
                    )

                    else -> Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = LocalContentColor.current
                    )
                }
            }
        },
    )

    // Favourite
    val favourite = state.favourite
    val facade = LocalSystemFacade.current
    LottieAnimButton(
        id = R.raw.lt_twitter_heart_filled_unfilled,
        onClick = { state.toggleFav(); facade.launchReviewFlow() },
        scale = 3.5f,
        progressRange = 0.13f..0.95f,
        duration = 800,
        atEnd = !favourite
    )

    // More
    More(state = state, onRequest)
}

/**
 * Composable function that renders the background of the console, providing a customizable background and content area.
 *
 * This function handles different background styles, and animations for smooth transitions.
 *
 * @param style The desired background style. Use one of the values from the [Background] annotation.
 * @param modifier [Modifier] to apply to the background element.
 */
@Composable
private fun Background(
    @Background style: Int,
    modifier: Modifier = Modifier
) {
    when (style) {
        // Use black color for video background and animate changes smoothly
        BACKGROUND_VIDEO_SURFACE, DEFAULT_BACKGROUND -> {
            // Animate color changes for visual transitions
            val color by animateColorAsState(
                targetValue = if (style == BACKGROUND_VIDEO_SURFACE) Color.Black else Material.colors.background,
                label = "Background color Change."
            )
            // Create the background with the determined color
            Spacer(modifier = modifier.background(color))
        }

        else -> TODO("Not Implemented yet background style: $style")
    }
}


@Composable
private fun Message(
    message: CharSequence?,
    modifier: Modifier = Modifier
) {
    // Early return when message is empty
    if (message == null) return Unit
    Label(
        text = message,
        modifier = modifier
            .shadow(ContentElevation.medium, Material.shapes.small2, true)
            .background(Color.Black)
            .padding(horizontal = ContentPadding.normal, vertical = ContentPadding.medium)
    )
}

/**
 * Composable function that renders the main content of the console, managing layout, media
 * playback, and user interactions.
 *
 * @param state The current state of the console, providing access to playback information, UI
 *              configuration, and content metadata.
 * @param constraints A [Constraints] that defines the layout constraints for the content,
 *                    ensuring proper positioning and adaptability.
 * @param onRequest A callback function that handles requests from the main content, such as showing
 *                  dialogs or handling back button presses.
 * @param modifier [Modifier] to apply to the main content element.
 *
 */
@Composable
private fun MainContent(
    state: Console,
    constraints: Constraints,
    onRequest: (request: @Request Int) -> Boolean,
    modifier: Modifier = Modifier
) = ConstraintLayout(
    constraintSet = constraints.value,
    modifier = modifier,
    //TODO - Setting this true causes crash; Surely in future.
    animateChanges = false
) {
    // Get the current navigation controller
    val navController = LocalNavController.current
    // FixMe: The PlayerView, rendering on a separate SurfaceView, interferes with animations.
    // This workaround flags the removal of PlayerView beforehand to prevent animation interference.
    // A mutable state variable that indicates whether to remove the PlayerView or not
    var removePlayerView by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Define the navigation action when the back button is pressed
    val onNavigateBack: () -> Unit = onNavigateBack@{
        // Early return if the request was handled by the parent
        if (onRequest(REQUEST_HANDLE_BACK_PRESS))
            return@onNavigateBack
        // Check if the activity has orientation lock enabled - unlock it.
        if (context.activity.isOrientationLocked)
            context.activity.toggleRotationLock()
        else {
            // Flag to remove the PlayerView and navigate up in the navigation controller
            removePlayerView = true
            navController.navigateUp()
        }
    }
    // Set up BackHandler with the defined onNavigateBack action
    BackHandler(onBack = onNavigateBack)
    // Window Style
    // Each of these depend on background
    // The background color of the window
    val background: Int // The accent color of the window.
    val accent: Color   // The content color of the window.
    val contentColor: Color // The content color over the background.
    val isAppearanceLightSystemBars: Boolean // Whether the status bar and navigation bar should use light colors or not
    // Check if the console is playing a video or not
    val isVideo = state.isVideo
    when (isVideo) {
        true -> {
            background = BACKGROUND_VIDEO_SURFACE
            accent = Color.SignalWhite
            contentColor = Color.SignalWhite
            isAppearanceLightSystemBars = false
        }

        else -> {
            background = DEFAULT_BACKGROUND
            accent = Material.colors.primary
            contentColor = Material.colors.onSurface
            isAppearanceLightSystemBars = Material.colors.isAppearanceLightSystemBars
        }
    }
    // Change the appearance of System bars
    // Here I have decided the status bar will change only colors for this window.
    SideEffect {
        // Maybe use
        onRequest(if (isAppearanceLightSystemBars) REQUEST_REQUIRES_LIGHT_SYSTEM_BARS else REQUEST_REQUIRES_DARK_SYSTEM_BARS)
    }

    // Why use CompositionLocal?
    // This is because the children of this layout's components like Options etc. depend on this
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        // The Background of this component.
        Background(
            style = background,
            modifier = Modifier.layoutId(Constraints.ID_BACKGROUND)
        )

        // VideoSurface
        val isInInspectionMode = LocalInspectionMode.current
        if (isVideo && !removePlayerView && !isInInspectionMode)
            PlayerView(
                player = state.player,
                resizeMode = state.resizeMode,
                modifier = Modifier
                    .layoutId(Constraints.ID_VIDEO_SURFACE)
                    // TODO - Find Proper Place to store this logic.
                    // TODO - Add support for other gestures like seek, volume +/-, Brightness +/-
                    .pointerInput("tapGesture") {
                        var lastTapTime = -1L; var tapCount = 1 // Track double tap timing and count
                        detectTapGestures(
                            // Reset onTap
                            onTap = { tapCount = 1; lastTapTime = -1L; onRequest(REQUEST_TOGGLE_VISIBILITY) },
                            onLongPress = { onRequest(REQUEST_TOOGLE_LOCK) },
                            onDoubleTap = { offset ->
                                val visibility = state.visibility
                                val isLocked = visibility == Console.VISIBILITY_LOCKED
                                if (isLocked) {
                                    // Show message and return on lock
                                    onRequest(REQUEST_TOGGLE_VISIBILITY)
                                    return@detectTapGestures
                                }
                                // Ensure controller is hidden while tapping.
                                val visible = state.visibility == Console.VISIBILITY_VISIBLE
                                if (visible)
                                    state.visibility = Console.VISIBILITY_HIDDEN
                                val current = System.currentTimeMillis()
                                // Check if it is a continuous fast tap.
                                val isFastTap = current - lastTapTime < 600 // 600ms double tap window
                                if (isFastTap) tapCount++ else tapCount = 1
                                lastTapTime = current
                                val (width, _) = size
                                val (x, _) = offset // Extract tap position
                                // Maybe check if the device is in ltr/rtl
                                val isLeftTap = x < width / 2 // Determine tap side

                                // Calculate seek increment based on side
                                val increment = if (isLeftTap) -10_000L else +10_000L
                                Log.d(TAG, "onDoubleTap: width: $width, x: $x, isLeft: $isLeftTap, multiplier: $tapCount, visible: $visible")
                                state.seek(increment) // Perform seek
                                // Update message with multiplied seek time
                                state.message = "${if(isLeftTap) "-" else "+" }${tapCount * 10}s"
                            }
                        )
                    }
            )

        // Scrim; when current item is a video
        if (isVideo)
            Spacer(
                modifier = Modifier
                    .background(Color.Black.copy(0.35f))
                    .layoutId(Constraints.ID_SCRIM)
            )

        // Signature
        Text(
            text = stringResource(id = R.string.app_name),
            fontFamily = Settings.DancingScriptFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 70.sp,
            modifier = Modifier.layoutId(Constraints.ID_SIGNATURE),
            color = contentColor,
            maxLines = 1
        )

        // Close Button
        OutlinedButton2(
            onClick = onNavigateBack,
            modifier = Modifier
                .scale(0.8f)
                .layoutId(Constraints.ID_CLOSE_BTN),
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = Color.Transparent,
                accent
            ),
            contentPadding = PaddingValues(vertical = 16.dp),
            shape = RoundedCornerShape_24,
            content = {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Collapse"
                )
            },
        )

        // Artwork
        // TODO - Support different shapes, animation, effects, etc.
        com.prime.media.core.compose.Artwork(
            data = state.artworkUri,
            modifier = Modifier
                .layoutId(Constraints.ID_ARTWORK)
                .visualEffect(ImageBrush.NoiseBrush, 0.5f, true)
                .shadow(ContentElevation.medium, DefaultArtworkShape)
                .background(Material.colors.surface),
        )

        // Timer
        Label(
            text = state.position(LocalContentColor.current.copy(ContentAlpha.disabled)),
            modifier = Modifier.layoutId(Constraints.ID_POSITION),
            style = Material.typography.caption2,
            fontWeight = FontWeight.Bold
        )

        // Subtitle
        Label(
            text = state.subtitle ?: stringResource(id = R.string.unknown),
            style = Material.typography.caption2,
            modifier = Modifier.layoutId(Constraints.ID_SUBTITLE),
            color = contentColor
        )

        // Title
        Label(
            text = state.title ?: stringResource(id = R.string.unknown),
            fontSize = constraints.titleTextSize,// Maybe Animate
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .marque(Int.MAX_VALUE)
                .layoutId(Constraints.ID_TITLE),
            color = contentColor
        )

        // ProgressRow
        TimeBar(
            state = state,
            accent = accent,
            style = if (background == BACKGROUND_VIDEO_SURFACE) SEEKBAR_STYLE_SIMPLE else SEEKBAR_STYLE_WAVY,
            onRequest = onRequest,
            modifier = Modifier.layoutId(Constraints.ID_TIME_BAR)
        )

        // Controls
        Controls(
            state = state,
            style = if (background == BACKGROUND_VIDEO_SURFACE) PLAY_BUTTON_STYLE_SIMPLE else PLAY_BUTTON_STYLE_NEUMORPHIC,
            modifier = Modifier.layoutId(Constraints.ID_CONTROLS)
        )

        // Options
        Options(
            state = state,
            modifier = Modifier.layoutId(Constraints.ID_OPTIONS),
            onRequest = onRequest
        )

        // Message
        Message(
            message = state.message,
            modifier = Modifier.layoutId(Constraints.ID_MESSAGE)
        )
    }
}

/**
 * Checks if the shape of the window resembles a mobile phone in portrait mode.
 * @property isMobilePortrait a boolean value that indicates if the window is portrait or not
 */
private val WindowSize.isMobilePortrait
    get() = widthReach == Reach.Compact && widthReach < heightReach

/**
 * Constructs a two-pane strategy based on the window size and the gap width.
 * A two-pane strategy is a layout that divides the screen into two panes: a content pane and a details pane.
 * The content pane shows a list of items, and the details pane shows the details of the selected item.
 * The orientation and size of the two panes depend on the window size and the gap width.
 *
 * @param window the size of the window in which the two-pane strategy is applied.
 * @param gapWidth the width of the gap between the two panes.
 * @param restrinct an optional parameter that restricts the size of the details pane to this value if specified;
 * if 0.dp, it hides the details pane altogether;
 * if unspecified, it uses the default values based on the window size and orientation.
 * @return a [TwoPaneStrategy] object that represents the layout of the two panes.
 */
private fun TwoPaneStrategy(
    window: WindowSize,
    gapWidth: Dp,
    restrinct: Dp = Dp.Unspecified
): TwoPaneStrategy {
    // Check if the window is in mobile portrait mode.
    // A mobile portrait mode is one where width is compact and height is greater than width.
    val (width, height) = window.value
    return when (window.isMobilePortrait) {
        true -> {
            // In portrait mode, the two panes are stacked vertically.
            // The content pane height is 45% of the window height by default, but it is
            // constrained to be between 270.dp and 350.dp
            // If restrinct is specified, it overrides the default value.
            val splitFromTop = restrinct.takeOrElse { (height * 0.45f).coerceIn(270.dp, 400.dp) }
            VerticalTwoPaneStrategy(splitFromTop, !restrinct.isSpecified, gapWidth)
        }

        else -> {
            // In landscape mode, the two panes are side by side.
            // The content pane width is 60% of the window width by default, but it is constrained
            // to be between 320.dp and 500.dp
            // If restrinct is specified, it overrides the default value.
            val slitFromStart =
                restrinct.takeOrElse { width - (0.40f * width).coerceIn(320.dp, 500.dp) }
            HorizontalTwoPaneStrategy(slitFromStart, !restrinct.isSpecified, gapWidth)
        }
    }
}

/**
 * The minimum window size necessary to display the details section of the UI.
 *
 * This value ensures that the details have sufficient space to be presented
 * legibly and effectively. If the current window size falls below this threshold,
 * the details willn't be shown as part of ui to avoid visual clutter or potential usability issues.
 *
 * @property width The minimum required width in density-independent pixels (600.dp).
 * @property height The minimum required height in density-independent pixels (550.dp).
 */
private val minimumWindowSizeForDetails = DpSize(600.dp, 550.dp)

/**
 * The raius of the shapes uped in Two Panes.
 */
private const val TWO_PANE_RADIUS_PCT = 7

/**
 * Constant indicating that no details pane should be shown.
 */
private const val DETAILS_OF_NONE = -1

@Composable
fun Console(state: Console) {
    // Get the current window size to adapt the UI accordingly
    val windowSize by rememberUpdatedState(newValue = LocalWindowSize.current)
    // Track which component's details are currently being displayed
    var detailsOf by remember { mutableIntStateOf(DETAILS_OF_NONE) }
    Log.d(TAG, "windowSize: ${windowSize.value}")
    val view = LocalView.current
    val isInInspectionMode = LocalInspectionMode.current
    val controller = if (!isInInspectionMode)
        WindowCompat.getInsetsController((view.context as Activity).window, view)
    else
        null
    // Declare a function to handle incoming requests from the UI,
    // such as showing or hiding the details pane.
    val onRequest = onRequest@{ request:  Int ->
        // Log the incoming request value for debugging purposes
        Log.d(TAG, "onRequest code: $request")

        // Check for a common toggle scenario, where the user wants to show or hide the currently
        // displayed details pane.
        if (request == detailsOf) {
            detailsOf = DETAILS_OF_NONE
            return@onRequest true
        }

        // Check if the window size is sufficient for showing details as a second pane.
        val isDetailsRequest =
            request == REQUEST_SHOW_PROPERTIES || request == REQUEST_SHOW_PLAYING_QUEUE
        val size = windowSize.value
        if (isDetailsRequest && minimumWindowSizeForDetails.contains(size)) {
            Log.d(TAG, "Window is too small to display details: ${windowSize.value}")
            return@onRequest false
        }

        // Handle specific request types:
        when (request) {
            REQUEST_HANDLE_BACK_PRESS -> {
                // Consume request if locked.
                if (state.visibility == Console.VISIBILITY_LOCKED) {
                    state.message = "\uD83D\uDD12 Long click to unlock."
                    return@onRequest true
                }
                // Back press handling: check the current value of detailsOf
                // If no details are showing, do nothing and return false to indicate that the
                // request has not been handled by this function
                if (detailsOf == DETAILS_OF_NONE) false
                // Otherwise, hide the details and return true to indicate that the request has
                // been handled by this function
                else {
                    detailsOf = DETAILS_OF_NONE
                    true
                }
            }

            // Toggle Show/Hide Playng Queue
            REQUEST_SHOW_PLAYING_QUEUE -> {
                // Show the playing queue in the details pane
                detailsOf = request
                return@onRequest true
            }
            // Handle requests for light or dark system bars:
            REQUEST_REQUIRES_LIGHT_SYSTEM_BARS, REQUEST_REQUIRES_DARK_SYSTEM_BARS -> {
                val isAppearanceLightStatusBars = request != REQUEST_REQUIRES_DARK_SYSTEM_BARS
                controller?.isAppearanceLightStatusBars = isAppearanceLightStatusBars
                controller?.isAppearanceLightNavigationBars = isAppearanceLightStatusBars
                return@onRequest true
            }
            REQUEST_TOOGLE_LOCK -> {
                val isLocked = state.visibility == Console.VISIBILITY_LOCKED
                state.message = if (isLocked)  "\uD83D\uDD13 Unlocked" else  "\uD83D\uDD12 Locked"
                state.visibility = if (isLocked) Console.VISIBILITY_VISIBLE else Console.VISIBILITY_LOCKED
                // return consumed
                true
            }
            REQUEST_TOGGLE_VISIBILITY -> {
                val visibility = state.visibility
                val isLocked = visibility == Console.VISIBILITY_LOCKED
                if (isLocked){
                    state.message = "\uD83D\uDD12 Long click to Unlock"
                    return@onRequest true
                }
                // Don't entertain if it is exclusively set to always visible.
                if (visibility == Console.VISIBILITY_ALWAYS)
                    return@onRequest false
                val visible = visibility == Console.VISIBILITY_VISIBLE
                // Toggle Visibility
                if (visible)
                    state.visibility = Console.VISIBILITY_HIDDEN
                else
                    state.visibility = Console.VISIBILITY_VISIBLE
                // Return cosnumed.
                true
            }
            REQUEST_TOGGLE_ROTATION_LOCK -> {
                val activity = view.context.activity
                state.message = if (activity.isOrientationLocked)  "\uD83D\uDD13 Rotation" else  "\uD83D\uDD12 Rotation"
                activity.toggleRotationLock()
            }
            else -> error("Unsupported request: $request")  // Throw an error for unsupported requests
        }
    }
    //
    val isInTwoPaneMode = detailsOf != DETAILS_OF_NONE
    val radius by animateIntAsState(
        targetValue = if (isInTwoPaneMode) TWO_PANE_RADIUS_PCT else 0,
        label = ""
    )
    // The main content of the UI,
    // which can be moved around based on details being shown
    val content = remember {
        movableContentOf {
            // Determine whether to show the controller based on the visibility and the isVideo values
            // Always show the controller if not video
            val showController =
                state.visibility == Console.VISIBILITY_VISIBLE || state.visibility == Console.VISIBILITY_ALWAYS
            // Get the system bars insets as a DpRect
            val insets = (if (windowSize.isMobilePortrait && detailsOf != DETAILS_OF_NONE)
                WindowInsets.statusBars
            else WindowInsets.systemBars)
                .asDpRect
            // Why use BoxWithConstraints
            // Because the window doesn't always depend on the size i provided; since the size
            // can also depend on the split of the window.
            BoxWithConstraints {
                val newWindowSize = WindowSize(DpSize(maxWidth, maxHeight))
                val isVideo = state.isVideo
                // Calculate the constraints for the content based on the new window size, the insets,
                // the isVideo value, and the showController value
                val constraints = remember(isVideo, newWindowSize, showController, insets) {
                    calculateConstraintSet(newWindowSize, insets, isVideo, !showController)
                }
                // Set immersive mode based on the visibility state.
                SideEffect { controller?.immersiveMode(!showController) }
                // Display the main content with the given state, constraints, onRequest function, and
                // modifier
                MainContent(
                    state = state,
                    constraints = constraints,
                    onRequest = onRequest,
                    modifier = Modifier
                        .clip(RoundedCornerShape(radius))
                        .fillMaxSize()
                )
            }
        }
    }
    // Call to pause the screen when the user intends to leave the screen, and the current
    // content is a video.
    val owner = LocalLifecycleOwner.current
    // Determine the default appearance of light system bars based on user preferences and
    // material theme.
    val isAppearanceLightSystemBars = Material.colors.isAppearanceLightSystemBars
    // Use DisposableEffect to observe the lifecycle events of the current owner (typically the
    // current composable).
    DisposableEffect(key1 = owner) {
        // Define a LifecycleEventObserver to pause playback when the screen is paused.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && state.isVideo) {
                // Pause the playback when the screen is paused.
                state.isPlaying = false
            }
        }
        // Add the observer to the owner's lifecycle.
        owner.lifecycle.addObserver(observer)
        // Define cleanup logic when the effect is disposed of.
        onDispose {
            // Remove the observer from the owner's lifecycle.
            owner.lifecycle.removeObserver(observer)
            // Restore the default color appearance of system bars based on the theme.
            controller?.isAppearanceLightStatusBars = isAppearanceLightSystemBars
            controller?.isAppearanceLightNavigationBars = isAppearanceLightSystemBars
        }
    }

    // FixMe - This causes a little glitch.
    SideEffect {
        // Remove details if windowSize Changed and it doesn't respect minimumWindowSizeForDetails
        // constraint.
        if (isInTwoPaneMode && minimumWindowSizeForDetails.contains(windowSize.value))
            detailsOf = DETAILS_OF_NONE
    }

    // Check the value of detailsOf to determine whether to
    // show the content in full screen or in two panes
    // FixMe - I suspect moving content makes it not animate.
    if (!isInTwoPaneMode)
    // If detailsOf is none, return the content without using the TwoPane component
        return content()

    // Use TwoPane component to show the content in two panes
    val context = LocalContext.current
    TwoPane(
        // The first pane is the content, which can be moved around based on details being shown
        first = content,
        strategy = TwoPaneStrategy(windowSize, 10.dp),
        displayFeatures = if (isInInspectionMode) emptyList() else calculateDisplayFeatures(activity = context.activity),
        // The second pane is the details pane, which can show different content based on the
        // detailsOf value
        // Use the horizontal or vertical two pane strategy based on the orientation and the new window size
        second = {
            if (detailsOf == DETAILS_OF_NONE)
                return@TwoPane
            // Get the system bars insets as a padding values
            val padding = if (windowSize.isMobilePortrait)
                WindowInsets.navigationBars.asPaddingValues() + PaddingValues(horizontal = ContentPadding.normal)
            else
                WindowInsets.systemBars.asPaddingValues()
            // Use a Surface to display the playing queue
            Surface(
                // Apply the padding to the modifier and adjust the horizontal padding based on the orientation
                modifier = Modifier.padding(padding),
                // Use the ContentShape as the shape of the surface
                shape = RoundedCornerShape(radius),
                // Use the overlay color or the background color based on the lightness of
                // the material colors
                color = if (Material.colors.isLight)
                    Material.colors.backgroundColorAtElevation(1.dp)
                else
                    Material.colors.backgroundColorAtElevation(0.1.dp),
                // Use the onSurface color as the content color
                contentColor = Material.colors.onSurface,
                // Use the outline color as the border stroke or null based on the lightness
                // of the material colors
                border = if (Material.colors.isLight) BorderStroke(
                    0.2.dp,
                    Material.colors.primary
                ) else null,
                // Display the playing queue with the given state, onDismissRequest function,
                // and modifier
                content = {
                    // Check the detailsOf value to determine what to show in the details pane
                    if (detailsOf == REQUEST_SHOW_PLAYING_QUEUE)
                        PlayingQueue(
                            state = state,
                            onDismissRequest = { detailsOf = DETAILS_OF_NONE },
                            modifier = Modifier.fillMaxSize()
                        )
                }
            )
        }
    )
}

