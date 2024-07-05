package me.salby.podcasts.ui.subscriptions

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.salby.podcasts.R
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.ui.playerInsetHeight
import me.salby.podcasts.ui.theme.EmphasizedAccelerate
import me.salby.podcasts.ui.theme.EmphasizedDecelerate
import me.salby.podcasts.ui.theme.PodcastsTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CompactSubscriptionsScreen(
    uiState: SubscriptionsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToFeed: (Feed) -> Unit,
    onSelectFeed: (feedId: Int) -> Unit,
    onDeselectFeed: (feedId: Int) -> Unit,
    onUnsubscribe: (feedId: Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.subscriptions)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            val selectedFeeds = when (uiState) {
                is SubscriptionsUiState.EditFeeds -> uiState.selectedFeeds
                else -> emptyList()
            }
            AnimatedContent(
                targetState = selectedFeeds,
                label = "Edit bar visibility",
                transitionSpec = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(
                            400,
                            200,
                            easing = EmphasizedDecelerate
                        )
                    ) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(
                            200,
                            easing = EmphasizedAccelerate
                        )
                    )
                },
                contentKey = { it.isNotEmpty() }
            ) { feeds ->
                if (feeds.isNotEmpty()) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = playerInsetHeight(8.dp))
                    ) {
                        IconButton(
                            onClick = { feeds.forEach { onUnsubscribe(it) } }
                        ) {
                            Icon(
                                if (feeds.size > 1) Icons.Outlined.DeleteSweep else Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.subscriptions_bulk_unsubscribe)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            pluralStringResource(
                                R.plurals.selected_subscriptions,
                                feeds.size,
                                feeds.size
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Spacer(
                        Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = playerInsetHeight(8.dp))
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        val surfaceModifier = if (
            sharedTransitionScope != null &&
            animatedVisibilityScope != null
        ) {
            with(sharedTransitionScope) {
                Modifier
                    .sharedBounds(
                        rememberSharedContentState("subscriptions-surface"),
                        animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(alignment = Alignment.TopCenter),
                        clipInOverlayDuringTransition = OverlayClip(MaterialTheme.shapes.extraLarge)
                    )
            }
        } else Modifier

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Surface(
                modifier = surfaceModifier
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraLarge),
                color = MaterialTheme.colorScheme.surface
            ) {
                Crossfade(
                    targetState = uiState.isLoading,
                    label = "Skeleton visibility"
                ) { showSkeleton ->
                    if (showSkeleton) {
                        FeedsGridLayout(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            userScrollEnabled = false
                        ) {
                            items(10) {
                                BoxWithConstraints {
                                    Box(
                                        modifier = Modifier
                                            .size(maxWidth)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Crossfade(
                            targetState = uiState is SubscriptionsUiState.HasFeeds || uiState is SubscriptionsUiState.EditFeeds,
                            label = "Feeds visibility"
                        ) { showFeeds ->
                            if (showFeeds) {
                                FeedsGrid(
                                    uiState,
                                    onNavigateToFeed,
                                    onSelectFeed,
                                    onDeselectFeed,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    sharedTransitionScope, animatedVisibilityScope
                                )
                            } else {
                                Box {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedsGrid(
    uiState: SubscriptionsUiState,
    onNavigateToFeed: (Feed) -> Unit,
    onSelectFeed: (feedId: Int) -> Unit,
    onDeselectFeed: (feedId: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val haptics = LocalHapticFeedback.current

    val feeds = when (uiState) {
        is SubscriptionsUiState.HasFeeds -> uiState.feeds
        is SubscriptionsUiState.EditFeeds -> uiState.feeds
        else -> emptyList()
    }
    val selectedFeeds = when (uiState) {
        is SubscriptionsUiState.EditFeeds -> uiState.selectedFeeds
        else -> emptyList()
    }

    FeedsGridLayout(modifier = modifier, contentPadding = contentPadding) {
        items(feeds) { feed ->
            val itemModifier = if (
                sharedTransitionScope != null &&
                animatedVisibilityScope != null
            ) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        rememberSharedContentState("feed-${feed.id}"),
                        animatedVisibilityScope
                    )
                }
            } else Modifier

            val isSelected = selectedFeeds.contains(feed.id)
            Subscription(
                feed.image,
                feed.title,
                modifier = Modifier
                    .fillMaxSize()
                    .animateItem(),
                imageModifier = itemModifier,
                isSelected = isSelected,
                onClick = {
                    if (isSelected) {
                        onDeselectFeed(feed.id)
                    } else if (uiState is SubscriptionsUiState.EditFeeds) {
                        onSelectFeed(feed.id)
                    } else {
                        onNavigateToFeed(feed)
                    }
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isSelected) onDeselectFeed(feed.id) else onSelectFeed(feed.id)
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun Subscription(
    image: Any?,
    title: String,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val selectedTransition = updateTransition(
        targetState = isSelected,
        label = "Subscription is selected"
    )
    val borderWidth by selectedTransition.animateDp(label = "Border width") {
        if (it) 4.dp else 0.dp
    }
    val borderColor by selectedTransition.animateColor(label = "Border color") {
        if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0f)
    }
    val imagePadding by selectedTransition.animateDp(label = "Image inset padding") {
        if (it) 8.dp else 0.dp
    }
    val imageBorderRadius by selectedTransition.animateDp(label = "Image shape") {
        if (it) 8.dp else 16.dp
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                borderWidth,
                borderColor,
                MaterialTheme.shapes.large
            )
    ) {
        GlideImage(
            model = image,
            contentDescription = title,
            modifier = imageModifier
                .size(maxWidth)
                .padding(imagePadding)
                .clip(RoundedCornerShape(imageBorderRadius)),
            loading = placeholder(ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh)),
            failure = placeholder(ColorPainter(MaterialTheme.colorScheme.errorContainer))
        )

        Crossfade(
            targetState = isSelected,
            label = "Selected indicator visibility",
            modifier = Modifier.align(Alignment.TopEnd)
        ) { showIcon ->
            if (showIcon) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun FeedsGridLayout(
    modifier: Modifier = Modifier,
    numberOfColumns: Int = 2,
    gridState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(numberOfColumns),
        modifier = modifier,
        state = gridState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

class SubscriptionsUiStatePreviewParameterProvider :
    PreviewParameterProvider<SubscriptionsUiState> {
    override val values = sequenceOf(
        SubscriptionsUiState.HasFeeds(
            feeds = listOf(
                Feed(
                    id = 1,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                ),
                Feed(
                    id = 2,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                ),
                Feed(
                    id = 3,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                )
            ),
            isLoading = false
        ),
        SubscriptionsUiState.EditFeeds(
            feeds = listOf(
                Feed(
                    id = 1,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                ),
                Feed(
                    id = 2,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                ),
                Feed(
                    id = 3,
                    title = "Undskyld vi roder",
                    url = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/podcast.rss",
                    link = "https://www.r8dio.dk/",
                    description = "I denne udsendelsesrække følger vi tilblivelsen af r8Dio - Danmarks nye snakke-sludre-taleradio. Det er en dokumentaristisk behind-the-scenes føljeton, der giver et indblik i det store arbejde med at skabe en landsdækkende radio.",
                    author = "r8Dio",
                    image = "https://placehold.co/200x200/png",
                    language = "da",
                    isExplicit = false,
                    episodeCount = 211,
                    subscribed = null
                )
            ),
            selectedFeeds = listOf(1),
            isLoading = false
        ),
        SubscriptionsUiState.NoFeeds(true),
        SubscriptionsUiState.NoFeeds(false)
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SubscriptionsScreenPreview(
    @PreviewParameter(SubscriptionsUiStatePreviewParameterProvider::class) uiState: SubscriptionsUiState
) {
    PodcastsTheme {
        CompactSubscriptionsScreen(
            uiState = uiState,
            onNavigateBack = {},
            onNavigateToFeed = {},
            onSelectFeed = {},
            onDeselectFeed = {},
            onUnsubscribe = {}
        )
    }
}