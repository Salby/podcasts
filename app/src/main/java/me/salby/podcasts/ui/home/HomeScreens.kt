package me.salby.podcasts.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.salby.podcasts.LocalPlayer
import me.salby.podcasts.R
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.Progress
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import me.salby.podcasts.ui.DurationFormatter
import me.salby.podcasts.ui.FeedsGrid
import me.salby.podcasts.ui.Player
import me.salby.podcasts.ui.episode.EpisodeListItem
import me.salby.podcasts.ui.format
import me.salby.podcasts.ui.placeholder
import me.salby.podcasts.ui.theme.EmphasizedAccelerate
import me.salby.podcasts.ui.theme.EmphasizedDecelerate
import me.salby.podcasts.ui.theme.PodcastsTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun CompactHomeScreen(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSearchIsExpandedChange: (Boolean) -> Unit,
    onNavigateToFeed: (Feed) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val player = LocalPlayer.current

    Box {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                item { Spacer(Modifier.height(64.dp)) }

                item {
                    val subscriptionsModifier = if (
                        sharedTransitionScope != null &&
                        animatedVisibilityScope != null
                    ) {
                        with(sharedTransitionScope) {
                            Modifier
                                .sharedBounds(
                                    rememberSharedContentState("subscriptions-surface"),
                                    animatedVisibilityScope,
                                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                        alignment = Alignment.TopCenter
                                    ),
                                    placeHolderSize = SharedTransitionScope.PlaceHolderSize.animatedSize
                                )
                        }
                    } else Modifier

                    Box(modifier = Modifier.padding(HomeCardMargin)) {
                        Surface(
                            modifier = subscriptionsModifier.clip(MaterialTheme.shapes.large),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(R.string.subscriptions),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Crossfade(uiState, label = "Subscriptions") {
                                    when {
                                        it.isLoading -> SubscriptionsCarouselPlaceholder(
                                            modifier = Modifier.fillMaxWidth(),
                                            itemModifier = {
                                                Modifier.placeholder(true)
                                            }
                                        )

                                        it is HomeUiState.HasFeeds -> SubscriptionsCarousel(
                                            it.feeds, onNavigateToFeed,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        else -> SubscriptionsCarouselEmptyMessage(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("No subscriptions")
                                        }
                                    }
                                }
                                TextButton(
                                    onClick = onNavigateToSubscriptions,
                                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                                    enabled = uiState is HomeUiState.HasFeeds
                                ) {
                                    Text(stringResource(R.string.show_all))
                                }
                            }
                        }
                    }
                }

                item {
                    AnimatedContent(
                        targetState = uiState,
                        transitionSpec = {
                            scaleIn(
                                tween(400, 200, EmphasizedDecelerate),
                                .96f
                            ) + fadeIn(
                                tween(400, 200, EmphasizedDecelerate)
                            ) togetherWith scaleOut(
                                tween(200, easing = EmphasizedAccelerate),
                                targetScale = .96f
                            ) + fadeOut(
                                tween(200, easing = EmphasizedAccelerate)
                            )
                        },
                        modifier = Modifier.clip(MaterialTheme.shapes.large),
                        label = "Latest episode visibility",
                        contentKey = { it is HomeUiState.HasFeeds && it.latestEpisode != null }
                    ) { currentUiState ->
                        if (currentUiState is HomeUiState.HasFeeds && currentUiState.latestEpisode != null) {
                            LatestEpisode(
                                model = currentUiState.latestEpisode,
                                onPlay = {
                                    player.playEpisodeFromPosition(
                                        currentUiState.latestEpisode.episode.id,
                                        currentUiState.latestEpisode.progress.progress
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(HomeCardMargin)
                                    .placeholder(
                                        visible = currentUiState.isLoading,
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = MaterialTheme.shapes.large
                                    )
                            )
                        } else {
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(HomeCardMargin)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    HomeSearchBar(
                        uiState,
                        onQueryChange,
                        onSearchIsExpandedChange,
                        onNavigateToSearchResultFeed,
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center)
                            .sharedBounds(
                                rememberSharedContentState("feed-search"),
                                animatedVisibilityScope,
                                zIndexInOverlay = 2f,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                            )
                            .skipToLookaheadSize()
                    )
                }
            } else {
                HomeSearchBar(
                    uiState, onQueryChange, onSearchIsExpandedChange, onNavigateToSearchResultFeed
                )
            }
        }
    }
}

private val HomeCardMargin = PaddingValues(16.dp, 16.dp, 16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSearchBar(
    uiState: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSearchIsExpandedChange: (Boolean) -> Unit,
    onNavigateToSearchResultFeed: (Feed) -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = uiState.searchIsActive,
                onExpandedChange = onSearchIsExpandedChange,
                placeholder = { Text(stringResource(R.string.feed_search_hint)) },
                leadingIcon = {
                    AnimatedContent(
                        targetState = uiState.searchIsActive,
                        contentAlignment = Alignment.Center,
                        label = "Search indicator or navigation button"
                    ) {
                        if (it) {
                            IconButton(onClick = {
                                onQueryChange("")
                                onSearchIsExpandedChange(false)
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_close)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                    }
                },
                trailingIcon = {
                    if (uiState.query.trim().isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.search_clear)
                            )
                        }
                    }
                }
            )
        },
        expanded = uiState.searchIsActive,
        onExpandedChange = onSearchIsExpandedChange,
        modifier = modifier,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        FeedsGrid(
            feeds = uiState.searchResult,
            contentPadding = PaddingValues(16.dp),
            onClick = onNavigateToSearchResultFeed
        )
    }
}

class HomeUiStatePreviewParameterProvider : PreviewParameterProvider<HomeUiState> {
    override val values = sequenceOf(
        HomeUiState.HasFeeds(
            feeds = listOf(
                Feed(
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
                Feed(
                    id = 1,
                    title = "70mmr",
                    url = "https://anchor.fm/s/12d1fabc/podcast/rss",
                    link = "https://www.70mmpod.com",
                    description = "A podcast for movie fans, inspired by Letterboxd. (We're not experts.) Each week artist Danny Haas, spiritual advisor Protolexus, and journeyman podcaster Slim discuss a recently watched film together. A brand new theme each month. Their love for each other cannot be broken. Or can it? Merch available + new episodes every Monday.",
                    author = "70mm",
                    image = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/3057511/3057511-1648820616219-0adef97444f1.jpg",
                    language = "en",
                    isExplicit = false,
                    episodeCount = 255,
                    subscribed = null
                )
            ), // TODO: Add feeds for preview.
            latestEpisode = ProgressWithEpisode(
                progress = Progress(
                    id = 1,
                    progress = 1782.seconds.inWholeMilliseconds,
                    LocalDateTime.now(), LocalDateTime.now(), 2
                ),
                episode = Episode(
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
                )
            ),
            query = "",
            searchResult = emptyList(),
            searchIsActive = false,
            isLoading = false,
            messages = emptyList()
        ),
        HomeUiState.NoFeeds(
            query = "",
            searchResult = emptyList(),
            searchIsActive = false,
            isLoading = false,
            messages = emptyList()
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview(
    @PreviewParameter(HomeUiStatePreviewParameterProvider::class) uiState: HomeUiState
) {
    PodcastsTheme {
        CompactHomeScreen(
            uiState,
            onQueryChange = {},
            onSearchIsExpandedChange = {},
            onNavigateToFeed = {},
            onNavigateToSearchResultFeed = {},
            onNavigateToSubscriptions = {}
        )
    }
}