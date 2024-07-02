package me.salby.podcasts.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.launch
import me.salby.podcasts.CompositionPlayerState
import me.salby.podcasts.LocalPlayer
import me.salby.podcasts.ProvidePlayerState
import me.salby.podcasts.data.player.PlayerState
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.ui.theme.EmphasizedAccelerate
import me.salby.podcasts.ui.theme.EmphasizedDecelerate
import me.salby.podcasts.ui.theme.PodcastsTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Player(modifier: Modifier = Modifier, isExpanded: Boolean = false) {
    val player = LocalPlayer.current
    val playerScope = rememberCoroutineScope()

    @Suppress("NAME_SHADOWING")
    var isExpanded by remember { mutableStateOf(isExpanded) }

    val animationProgress = remember { Animatable(initialValue = if (isExpanded) 1f else 0f) }
    val finalBackProgress = remember { mutableFloatStateOf(Float.NaN) }
    val firstBackEvent = remember { mutableStateOf<BackEventCompat?>(null) }
    val currentBackEvent = remember { mutableStateOf<BackEventCompat?>(null) }

    LaunchedEffect(isExpanded) {
        val animationInProgress = animationProgress.value > 0 && animationProgress.value < 1
        val animationSpec =
            if (animationInProgress) AnimationPredictiveBackExitFloatSpec
            else if (isExpanded) AnimationEnterFloatSpec
            else AnimationExitFloatSpec
        val targetValue = if (isExpanded) 1f else 0f
        if (animationProgress.value != targetValue) {
            animationProgress.animateTo(targetValue, animationSpec)
        }
        if (!isExpanded) {
            finalBackProgress.floatValue = Float.NaN
            firstBackEvent.value = null
            currentBackEvent.value = null
        }
    }

    val mutatorMutex = remember { MutatorMutex() }
    PredictiveBackHandler(enabled = isExpanded) { progress ->
        mutatorMutex.mutate {
            try {
                finalBackProgress.floatValue = Float.NaN
                progress.collect { backEvent ->
                    if (firstBackEvent.value == null) {
                        firstBackEvent.value = backEvent
                    }
                    currentBackEvent.value = backEvent
                    val interpolatedProgress = PredictiveBackEasing.transform(backEvent.progress)
                    animationProgress.snapTo(targetValue = 1 - interpolatedProgress)
                }
                finalBackProgress.floatValue = animationProgress.value
                isExpanded = false
            } catch (e: CancellationException) {
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = AnimationPredictiveBackExitFloatSpec
                )
                finalBackProgress.floatValue = Float.NaN
                firstBackEvent.value = null
                currentBackEvent.value = null
            }
        }
    }

    val onPlayPause = {
        playerScope.launch {
            if (player.state.isLoading || player.state !is PlayerState.Active) {
                return@launch
            }
            if (player.state.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    val onSeekNext = {
        playerScope.launch {
            if (player.state.isLoading || player.state !is PlayerState.Active) {
                return@launch
            }
            player.seekNext()
        }
    }

    val onSeekPrevious = {
        playerScope.launch {
            if (player.state.isLoading || player.state !is PlayerState.Active) {
                return@launch
            }
            player.seekPrevious()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        if (animationProgress.value > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = animationProgress.value / 6))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = isExpanded
                    ) { isExpanded = false }
            )
        }

        AnimatedContent(
            targetState = player.state is PlayerState.Active && !player.isHidden,
            transitionSpec = {
                (fadeIn(
                    animationSpec = tween(
                        600,
                        delayMillis = 150,
                        easing = EmphasizedDecelerate
                    )
                ) +
                        scaleIn(
                            initialScale = 0.9f,
                            animationSpec = tween(
                                600,
                                delayMillis = 150,
                                easing = EmphasizedDecelerate
                            )
                        ))
                    .togetherWith(
                        fadeOut(
                            animationSpec = tween(
                                150,
                                easing = EmphasizedAccelerate
                            )
                        )
                    )
            },
            contentAlignment = Alignment.Center,
            label = "Player visibility"
        ) { showPlayer ->
            if (showPlayer) {
//                PlayerLayout(
//                    animationProgress,
//                    finalBackProgress,
//                    firstBackEvent,
//                    currentBackEvent,
//                    minimizedContent = {
//                        MinimizedPlayer(
//                            it,
//                            { onPlayPause() },
//                            modifier = Modifier
//                                .clip(MaterialTheme.shapes.extraLarge)
//                                .alpha(if (!isExpanded) 1f - animationProgress.value else 0f)
//                                .clickable { isExpanded = true }
//                        )
//                    },
//                    expandedContent = {
//                        ExpandedPlayer(
//                            it,
//                            { onPlayPause() },
//                            { onSeekPrevious() },
//                            { onSeekNext() },
//                            animationProgress,
//                            currentBackEvent,
//                            finalBackProgress
//                        ) { isExpanded = false }
//                    },
//                    surfaceContent = {
//                        Surface(
//                            shape = MaterialTheme.shapes.extraLarge,
//                            color = MaterialTheme.colorScheme.surfaceContainerHighest
//                        ) {}
//                    }
//                )
                newPlayerLayout(
                    isExpanded = isExpanded,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp),
                    minimizedContent = { state, animatedVisibilityScope, sharedTransitionScope ->
                        MinimizedPlayer(
                            state,
                            { onPlayPause() },
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .alpha(if (!isExpanded) 1f - animationProgress.value else 0f)
                                .clickable { isExpanded = true },
                            animatedVisibilityScope,
                            sharedTransitionScope
                        )
                    },
                    expandedContent = { state, animatedVisibilityScope, sharedTransitionScope ->
                        ExpandedPlayer(
                            state,
                            { onPlayPause() },
                            { onSeekPrevious() },
                            { onSeekNext() },
                            animationProgress,
                            currentBackEvent,
                            finalBackProgress,
                            onMinimizePlayer = { isExpanded = false },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun newPlayerLayout(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    minimizedContent: @Composable (PlayerState.Active, AnimatedVisibilityScope, SharedTransitionScope) -> Unit,
    expandedContent: @Composable (PlayerState.Active, AnimatedVisibilityScope, SharedTransitionScope) -> Unit
) {
    val player = LocalPlayer.current

    if (player.state is PlayerState.Active) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = isExpanded,
                    label = ""
                ) { showExpandedPlayer ->
                    if (showExpandedPlayer) {
                        expandedContent(player.state, this, this@SharedTransitionLayout)
                    } else {
                        minimizedContent(player.state, this, this@SharedTransitionLayout)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerLayout(
    animationProgress: Animatable<Float, AnimationVector1D>,
    finalBackProgress: MutableFloatState,
    firstBackEvent: MutableState<BackEventCompat?>,
    currentBackEvent: MutableState<BackEventCompat?>,
    modifier: Modifier = Modifier,
    surfaceContent: @Composable () -> Unit,
    minimizedContent: @Composable (PlayerState.Active) -> Unit,
    expandedContent: @Composable (PlayerState.Active) -> Unit
) {
    val player = LocalPlayer.current
    val playerState = player.state as PlayerState.Active
    val minimizedInsets = WindowInsets.safeContent
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val unconsumedInsets = remember { MutableWindowInsets() }
    Layout(
        modifier = modifier
            .zIndex(2f)
            .consumeWindowInsets(unconsumedInsets),
        content = {
            Box(Modifier.layoutId("surface"), propagateMinConstraints = true) {
                surfaceContent()
            }
            Box(Modifier.layoutId("expanded"), propagateMinConstraints = true) {
                expandedContent(playerState)
            }
            Box(Modifier.layoutId("minimized"), propagateMinConstraints = true) {
                minimizedContent(playerState)
            }
        }
    ) { measurables, constraints ->
        val surfaceMeasurable = measurables.fastFirst { it.layoutId == "surface" }
        val expandedMeasurable = measurables.fastFirst { it.layoutId == "expanded" }
        val minimizedMeasurable = measurables.fastFirst { it.layoutId == "minimized" }

        val minimizedBottomPadding = minimizedInsets.getBottom(density)
        val expandedBottomPadding = minimizedBottomPadding + 144.dp.roundToPx()
        val bottomPadding = lerp(
            minimizedBottomPadding,
            expandedBottomPadding,
            animationProgress.value
        )

        val defaultStartWidth = constraints
            .constrainWidth(minimizedMeasurable.maxIntrinsicWidth(constraints.maxHeight)) -
                minimizedInsets.getLeft(density, layoutDirection).dp.roundToPx() -
                minimizedInsets.getRight(density, layoutDirection).dp.roundToPx()
        val defaultStartHeight = constraints
            .constrainHeight(minimizedMeasurable.minIntrinsicHeight(constraints.maxWidth))

        val expandedWidth = constraints.maxWidth - (48.dp.roundToPx())
        val defaultExpandedHeight = constraints
            .constrainHeight(expandedMeasurable.minIntrinsicHeight(constraints.maxWidth))

        val predictiveBackStartWidth =
            (expandedWidth * (9f / 10f)).roundToInt()
        val predictiveBackStartHeight =
            (defaultExpandedHeight * (9f / 10f)).roundToInt()
        val predictiveBackMultiplier = calculatePredictiveBackMultiplier(
            currentBackEvent.value,
            animationProgress.value,
            finalBackProgress.floatValue
        )

        val startWidth =
            lerp(defaultStartWidth, predictiveBackStartWidth, predictiveBackMultiplier)
        val startHeight = lerp(
            defaultStartHeight,
            predictiveBackStartHeight,
            predictiveBackMultiplier
        )

        val minWidth = lerp(startWidth, expandedWidth, animationProgress.value)
        val height = lerp(startHeight, defaultExpandedHeight, animationProgress.value)

        val minimizedPlaceable = minimizedMeasurable.measure(
            Constraints(
                minWidth = minWidth,
                maxWidth = expandedWidth,
                minHeight = defaultStartHeight,
                maxHeight = defaultStartHeight
            )
        )
        val width = minimizedPlaceable.width

        val expandedPlaceable = expandedMeasurable.measure(
            Constraints(
                minWidth = width,
                maxWidth = width,
                minHeight = 0,
                maxHeight = if (constraints.hasBoundedHeight) {
                    (constraints.maxHeight - defaultStartHeight)
                        .coerceAtLeast(0)
                } else {
                    constraints.maxHeight
                }
            )
        )
        val surfacePlaceable = surfaceMeasurable
            .measure(Constraints.fixed(width, height))

        layout(width, height) {
            val minOffsetMargin = 8.dp.roundToPx()
            val predictiveBackOffsetX = calculatePredictiveBackOffsetX(
                constraints = constraints,
                minMargin = minOffsetMargin,
                currentBackEvent = currentBackEvent.value,
                layoutDirection = layoutDirection,
                progress = animationProgress.value,
                predictiveBackMultiplier = predictiveBackMultiplier,
            )
            val predictiveBackOffsetY = calculatePredictiveBackOffsetY(
                constraints = constraints,
                minMargin = minOffsetMargin,
                currentBackEvent = currentBackEvent.value,
                firstBackEvent = firstBackEvent.value,
                height = height,
                maxOffsetY = 24.dp.roundToPx(),
                predictiveBackMultiplier = predictiveBackMultiplier,
            ) - bottomPadding

            surfacePlaceable.placeRelative(
                predictiveBackOffsetX,
                predictiveBackOffsetY
            )
            minimizedPlaceable.placeRelative(
                predictiveBackOffsetX,
                predictiveBackOffsetY
            )
            expandedPlaceable.placeRelative(
                predictiveBackOffsetX,
                predictiveBackOffsetY
            )
        }
    }
}

private fun calculatePredictiveBackMultiplier(
    currentBackEvent: BackEventCompat?,
    progress: Float,
    finalBackProgress: Float
) = when {
    currentBackEvent == null -> 0f // Not in predictive back at all.
    finalBackProgress.isNaN() -> 1f // User is currently swiping predictive back.
    finalBackProgress <= 0 -> 0f // Safety check for divide by zero.
    else -> progress / finalBackProgress // User has released predictive back swipe.
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpandedPlayer(
    playerState: PlayerState.Active,
    onPlayPauseButtonClick: () -> Unit,
    onSeekPreviousClick: () -> Unit,
    onSeekNextClick: () -> Unit,
    animationProgress: Animatable<Float, AnimationVector1D>,
    currentBackEvent: MutableState<BackEventCompat?>,
    finalBackProgress: MutableFloatState,
    modifier: Modifier = Modifier,
    onMinimizePlayer: (() -> Unit)? = null,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val player = LocalPlayer.current

    var progressSliderValue by remember { mutableFloatStateOf(playerState.currentPosition.toFloat()) }
    val timeLeft =
        playerState.currentEpisode.duration - progressSliderValue.toLong().milliseconds.inWholeSeconds.seconds
    var progressSliderValueIsChanging by remember { mutableStateOf(false) }

    LaunchedEffect(playerState.currentPosition, progressSliderValueIsChanging) {
        if (!progressSliderValueIsChanging) {
            progressSliderValue = playerState.currentPosition.toFloat()
        }
    }

    val playButtonAlpha by remember(currentBackEvent, animationProgress) {
        derivedStateOf {
            if (currentBackEvent.value != null) {
                if (animationProgress.value > .25f) {
                    (animationProgress.value - .25f) * 10
                } else 0f
            } else if (animationProgress.value > .9f) {
                (animationProgress.value - .9f) * 10
            } else 0f
        }
    }

    val playButtonScale by remember(currentBackEvent, animationProgress) {
        derivedStateOf {
            if (currentBackEvent.value != null) {
                lerp(.5f, 1f, animationProgress.value)
            } else {
                animationProgress.value
            }
        }
    }

    val playButtonMarginTop = lerp(0, 16, animationProgress.value)

    val alpha by remember(currentBackEvent, animationProgress) {
        derivedStateOf {
            when {
                currentBackEvent.value == null -> if (animationProgress.value > .5f) {
                    (animationProgress.value - .5f) * 10
                } else 0f

                finalBackProgress.floatValue.isNaN() || finalBackProgress.floatValue <= 0 -> 1f
                else -> 0f
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(alpha = alpha)
        ) {
            Text(
                playerState.currentFeed.title,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )

            onMinimizePlayer?.let {
                IconButton(onClick = it, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        BoxWithConstraints {
            val imageSize = maxWidth - 112.dp

            with(sharedTransitionScope) {
                GlideImage(
                    model = playerState.currentEpisode.image,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState("playerimage"),
                            animatedVisibilityScope
                        )
                        .size(imageSize)
                        .clip(RoundedCornerShape(20.dp)),
                    loading = placeholder(ColorPainter(MaterialTheme.colorScheme.secondary)),
                    failure = placeholder(ColorPainter(MaterialTheme.colorScheme.error))
                )
            }
        }

        Text(
            playerState.currentEpisode.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
                .fillMaxWidth()
                .graphicsLayer(alpha = alpha)
        )

        Column(
            modifier = Modifier.graphicsLayer(alpha = alpha)
        ) {
            Slider(
                enabled = !playerState.isLoading,
                value = progressSliderValue,
                valueRange = 0f..playerState.currentEpisode.duration.inWholeMilliseconds.toFloat(),
                onValueChange = { progressSliderValue = it },
                onValueChangeFinished = { player.setPosition(progressSliderValue.toLong()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { progressSliderValueIsChanging = true },
                            onDragEnd = { progressSliderValueIsChanging = false }
                        ) { _, _ -> }
                    },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = .38f)
                )
            )
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.outline,
                LocalTextStyle provides MaterialTheme.typography.labelMedium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        progressSliderValue.toLong().milliseconds.inWholeSeconds.seconds
                            .format(DurationFormatter.POSITION)
                    )
                    Text(
                        "-${timeLeft.format(DurationFormatter.POSITION)}"
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = playButtonMarginTop.dp)
                .padding(bottom = 16.dp)
                .graphicsLayer(alpha = playButtonAlpha),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSeekPreviousClick) {
                Icon(
                    Icons.Outlined.SkipPrevious,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            LargeFloatingActionButton(
                onClick = onPlayPauseButtonClick,
                modifier = Modifier.graphicsLayer(
                    scaleX = playButtonScale,
                    scaleY = playButtonScale,
                    transformOrigin = TransformOrigin.Center
                ),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                AnimatedContent(
                    targetState = playerState.isPlaying to playerState.isLoading,
                    contentAlignment = Alignment.Center,
                    label = "Play or pause icon"
                ) { (isPlaying, isLoading) ->
                    if (isLoading && !isPlaying) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else if (isPlaying) {
                        Icon(
                            Icons.Outlined.Pause,
                            contentDescription = null,
                            Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                        )
                    }
                }
            }

            IconButton(onClick = onSeekNextClick) {
                Icon(
                    Icons.Outlined.SkipNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MinimizedPlayer(
    playerState: PlayerState.Active,
    onPlayPauseButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val timeLeft =
        playerState.currentEpisode.duration - playerState.currentPosition.milliseconds.inWholeSeconds.seconds

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            with(sharedTransitionScope) {
                GlideImage(
                    model = playerState.currentEpisode.image,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState("playerimage"),
                            animatedVisibilityScope
                        )
                        .size(62.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    loading = placeholder(ColorPainter(MaterialTheme.colorScheme.secondary)),
                    failure = placeholder(ColorPainter(MaterialTheme.colorScheme.error))
                )
            }
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        playerState.currentEpisode.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth(.75f)
                    )
                    Text(
                        if (playerState.isLoading) "—" else "-${
                            timeLeft.format(
                                DurationFormatter.POSITION
                            )
                        }",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                FilledIconButton(
                    onClick = onPlayPauseButtonClick,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    AnimatedContent(
                        targetState = playerState.isPlaying to playerState.isLoading,
                        contentAlignment = Alignment.Center,
                        label = "Play or pause icon"
                    ) { (showPauseIcon, isLoading) ->
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else if (showPauseIcon) {
                            Icon(Icons.Outlined.Pause, contentDescription = null)
                        } else {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            LinearProgressIndicator(
                progress = {
                    if (playerState.isLoading) {
                        0f
                    } else {
                        (playerState.currentPosition.toFloat() / playerState.currentEpisode.duration.inWholeMilliseconds.toFloat())
                            .coerceIn(
                                0f,
                                playerState.currentEpisode.duration.inWholeMilliseconds.toFloat()
                            )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 24.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                drawStopIndicator = {}
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun calculatePredictiveBackOffsetX(
    constraints: Constraints,
    minMargin: Int,
    currentBackEvent: BackEventCompat?,
    layoutDirection: LayoutDirection,
    progress: Float,
    predictiveBackMultiplier: Float
): Int {
    if (currentBackEvent == null || predictiveBackMultiplier == 0f) {
        return 0
    }
    val directionMultiplier = if (currentBackEvent.swipeEdge == BackEventCompat.EDGE_LEFT) 1 else -1
    val rtlMultiplier = if (layoutDirection == LayoutDirection.Ltr) 1 else -1
    val maxOffsetX =
        (constraints.maxWidth * (1f / 20f)) - minMargin
    val interpolatedOffsetX = maxOffsetX * (1 - progress)
    return (interpolatedOffsetX * predictiveBackMultiplier * directionMultiplier * rtlMultiplier)
        .roundToInt()
}

private fun calculatePredictiveBackOffsetY(
    constraints: Constraints,
    minMargin: Int,
    currentBackEvent: BackEventCompat?,
    firstBackEvent: BackEventCompat?,
    height: Int,
    maxOffsetY: Int,
    predictiveBackMultiplier: Float
): Int {
    if (firstBackEvent == null || currentBackEvent == null || predictiveBackMultiplier == 0f) {
        return 0
    }
    val availableVerticalSpace = max(0, (constraints.maxHeight - height) / 2 - minMargin)
    val adjustedMaxOffsetY = min(availableVerticalSpace, maxOffsetY)
    val yDelta = currentBackEvent.touchY - firstBackEvent.touchY
    val yProgress = abs(yDelta) / constraints.maxHeight
    val directionMultiplier = sign(yDelta)
    val interpolatedOffsetY = lerp(0, adjustedMaxOffsetY, yProgress)
    return (interpolatedOffsetY * predictiveBackMultiplier * directionMultiplier).roundToInt()
}

// Animation specs
private const val AnimationEnterDurationMillis: Int = 600
private const val AnimationExitDurationMillis: Int = 450
private const val AnimationDelayMillis: Int = 100
private val AnimationEnterEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val AnimationExitEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val AnimationEnterFloatSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = AnimationEnterDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationEnterEasing,
)
private val AnimationExitFloatSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = AnimationExitDurationMillis,
    delayMillis = AnimationDelayMillis,
    easing = AnimationExitEasing,
)
private val AnimationPredictiveBackExitFloatSpec: FiniteAnimationSpec<Float> = tween(
    durationMillis = AnimationExitDurationMillis,
    easing = AnimationExitEasing,
)
private val PredictiveBackEasing: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

class PlayerStatePreviewParameterProvider : PreviewParameterProvider<PlayerState> {
    override val values = sequenceOf(
        PlayerState.Active(
            currentEpisode = Episode(
                id = 2,
                title = "Russere og den lille r8Diomus",
                description = "Afsnit 211. Klavs og Emil skal holde et skæbnesvangert møde med en udenlandsk interessent, men inden da ringer Klavs til Mickey Meny for at beklage en lille hændelse under lanceringsfesten for den nye øl, Bøvøl. Alle har ellers været enige om, at det var en fantastisk begivenhed og genialt af Mickey at mixe en hundeudstilling, musik og Bøvøl i måske én af de største Meny’er i verden.</p>\n<p >Herefter giver Rasmus Bruun en status. Han er efterhånden ved at være tilbage i topform. Dog viser det sig senere, at t...",
                published = LocalDateTime.ofEpochSecond(1713499200, 0, ZoneOffset.UTC),
                source = "https://traffic.omny.fm/d/clips/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/8dcbf36c-d9be-4d28-9306-b14f013dd275/audio.mp3?utm_source=Podcast&in_playlist=6af053db-da47-47dc-aee5-abca00c013d7",
                sourceType = "audio/mpeg",
                sourceLength = 46997472,
                duration = 2935.seconds,
                isExplicit = false,
                episode = 211,
                season = 8,
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                feedId = 1262243
            ),
            currentFeed = Feed(
                id = 1,
                title = "r8Dio - Undskyld vi roder",
                url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                link = "https://www.r8dio.dk/",
                description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                author = "r8Dio",
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                language = "da",
                isExplicit = false,
                episodeCount = 211,
                subscribed = null
            ),
            isPlaying = false,
            currentPosition = 1423.seconds.inWholeMilliseconds,
            isLoading = false
        ),
        PlayerState.Active(
            currentEpisode = Episode(
                id = 2,
                title = "Russere og den lille r8Diomus",
                description = "Afsnit 211. Klavs og Emil skal holde et skæbnesvangert møde med en udenlandsk interessent, men inden da ringer Klavs til Mickey Meny for at beklage en lille hændelse under lanceringsfesten for den nye øl, Bøvøl. Alle har ellers været enige om, at det var en fantastisk begivenhed og genialt af Mickey at mixe en hundeudstilling, musik og Bøvøl i måske én af de største Meny’er i verden.</p>\n<p >Herefter giver Rasmus Bruun en status. Han er efterhånden ved at være tilbage i topform. Dog viser det sig senere, at t...",
                published = LocalDateTime.ofEpochSecond(1713499200, 0, ZoneOffset.UTC),
                source = "https://traffic.omny.fm/d/clips/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/8dcbf36c-d9be-4d28-9306-b14f013dd275/audio.mp3?utm_source=Podcast&in_playlist=6af053db-da47-47dc-aee5-abca00c013d7",
                sourceType = "audio/mpeg",
                sourceLength = 46997472,
                duration = 2935.seconds,
                isExplicit = false,
                episode = 211,
                season = 8,
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                feedId = 1262243
            ),
            currentFeed = Feed(
                id = 1,
                title = "r8Dio - Undskyld vi roder",
                url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                link = "https://www.r8dio.dk/",
                description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                author = "r8Dio",
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                language = "da",
                isExplicit = false,
                episodeCount = 211,
                subscribed = null
            ),
            isPlaying = true,
            currentPosition = 1423.seconds.inWholeMilliseconds,
            isLoading = false
        ),
        PlayerState.Active(
            currentEpisode = Episode(
                id = 2,
                title = "Russere og den lille r8Diomus",
                description = "Afsnit 211. Klavs og Emil skal holde et skæbnesvangert møde med en udenlandsk interessent, men inden da ringer Klavs til Mickey Meny for at beklage en lille hændelse under lanceringsfesten for den nye øl, Bøvøl. Alle har ellers været enige om, at det var en fantastisk begivenhed og genialt af Mickey at mixe en hundeudstilling, musik og Bøvøl i måske én af de største Meny’er i verden.</p>\n<p >Herefter giver Rasmus Bruun en status. Han er efterhånden ved at være tilbage i topform. Dog viser det sig senere, at t...",
                published = LocalDateTime.ofEpochSecond(1713499200, 0, ZoneOffset.UTC),
                source = "https://traffic.omny.fm/d/clips/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/8dcbf36c-d9be-4d28-9306-b14f013dd275/audio.mp3?utm_source=Podcast&in_playlist=6af053db-da47-47dc-aee5-abca00c013d7",
                sourceType = "audio/mpeg",
                sourceLength = 46997472,
                duration = 2935.seconds,
                isExplicit = false,
                episode = 211,
                season = 8,
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                feedId = 1262243
            ),
            currentFeed = Feed(
                id = 1,
                title = "r8Dio - Undskyld vi roder",
                url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                link = "https://www.r8dio.dk/",
                description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                author = "r8Dio",
                image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                language = "da",
                isExplicit = false,
                episodeCount = 211,
                subscribed = null
            ),
            isPlaying = true,
            currentPosition = 1423.seconds.inWholeMilliseconds,
            isLoading = true
        ),
        PlayerState.None(isLoading = false)
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExpandedPlayerPreview(
    @PreviewParameter(PlayerStatePreviewParameterProvider::class) playerState: PlayerState
) {
    ProvidePlayerState(CompositionPlayerState(playerState)) {
        PodcastsTheme {
            Player(isExpanded = true)
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MinimizedPlayerPreview(
    @PreviewParameter(PlayerStatePreviewParameterProvider::class) playerState: PlayerState
) {
    ProvidePlayerState(CompositionPlayerState(playerState)) {
        PodcastsTheme {
            Player()
        }
    }
}