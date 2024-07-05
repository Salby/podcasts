package me.salby.podcasts.ui.feed

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Explicit
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.salby.podcasts.LocalPlayer
import me.salby.podcasts.R
import me.salby.podcasts.data.player.PlayerState
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.data.podcasts.model.Progress
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import me.salby.podcasts.ui.DurationFormatter
import me.salby.podcasts.ui.episode.EpisodeListItem
import me.salby.podcasts.ui.format
import me.salby.podcasts.ui.placeholder
import me.salby.podcasts.ui.playerInsetHeight
import me.salby.podcasts.ui.theme.EmphasizedAccelerate
import me.salby.podcasts.ui.theme.EmphasizedDecelerate
import me.salby.podcasts.ui.theme.PodcastsTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.seconds

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class,
    ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun CompactFeedScreen(
    uiState: FeedUiState,
    onNavigateBack: () -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val density = LocalDensity.current
    val player = LocalPlayer.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                BoxWithConstraints(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .statusBarsPadding()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .placeholder(
                            visible = uiState.isLoading,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = MaterialTheme.shapes.extraLarge
                        )
                ) {
                    val imageSize = maxWidth
                    val imageHeight =
                        (imageSize + with(density) { scrollBehavior.state.heightOffset.toDp() })
                            .coerceIn(64.dp, imageSize)

                    LaunchedEffect(imageSize) {
                        scrollBehavior.state.heightOffsetLimit =
                            with(density) { (64.dp - imageSize).toPx() }
                    }

                    val imageModifier = if (
                        sharedTransitionScope != null &&
                        animatedVisibilityScope != null &&
                        uiState is FeedUiState.HasFeed &&
                        scrollBehavior.state.collapsedFraction < 1.0
                    ) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState("feed-${uiState.feed.id}"),
                                animatedVisibilityScope,
                                placeHolderSize = SharedTransitionScope.PlaceHolderSize.animatedSize,
                                clipInOverlayDuringTransition = OverlayClip(MaterialTheme.shapes.extraLarge)
                            )
                        }
                    } else Modifier

                    Box(
                        modifier = imageModifier
                            .blur(lerp(0.dp, 24.dp, scrollBehavior.state.collapsedFraction))
                            .fillMaxWidth()
                            .height(imageHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        GlideImage(
                            model = when (uiState) {
                                is FeedUiState.HasFeed -> uiState.feed.image
                                else -> null
                            },
                            contentDescription = null,
                            modifier = Modifier
                                .size(imageSize),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop,
                            loading = placeholder(ColorPainter(MaterialTheme.colorScheme.surfaceContainerHighest)),
                            failure = placeholder(ColorPainter(MaterialTheme.colorScheme.secondaryContainer))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .padding(start = 8.dp)
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        if (navigationIcon != null) {
                            navigationIcon()
                        } else {
                            FilledIconButton(
                                onClick = onNavigateBack,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceBright
                                )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_back)
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = uiState,
                    modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                    transitionSpec = {
                        scaleIn(
                            tween(400, 200, EmphasizedDecelerate),
                            initialScale = .96f
                        ) + fadeIn(
                            tween(400, 200, EmphasizedDecelerate)
                        ) togetherWith scaleOut(
                            tween(200, easing = EmphasizedAccelerate),
                            targetScale = .96f
                        ) + fadeOut(
                            tween(200, easing = EmphasizedAccelerate)
                        )
                    },
                    label = "Last listened card visibility"
                ) { currentUiState ->
                    if (currentUiState is FeedUiState.HasFeed && currentUiState.lastListened != null) {
                        ContinueListeningCard(
                            model = currentUiState.lastListened,
                            onPlayEpisode = {
                                player.playEpisodeFromPosition(
                                    currentUiState.lastListened.episode.id,
                                    currentUiState.lastListened.progress.progress
                                )
                            },
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(Modifier.fillMaxWidth())
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Pane(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
            paneModifier = Modifier
                .fillMaxSize()
                .placeholder(
                    visible = uiState.isLoading,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            if (uiState is FeedUiState.HasFeed) {
                LazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
                    item {
                        Row(Modifier.padding(horizontal = 16.dp)) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    uiState.feed.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                if (uiState.feed.isExplicit) {
                                    Icon(
                                        Icons.Outlined.Explicit,
                                        contentDescription = stringResource(R.string.feed_explicit),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Text(
                                    uiState.feed.author,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    item {
                        FlowRow(
                            modifier = Modifier.padding(16.dp, 16.dp, 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedContent(
                                targetState = uiState.feed.subscribed != null,
                                contentAlignment = Alignment.Center,
                                label = "Subscribe button"
                            ) { isSubscribed ->
                                if (isSubscribed) {
                                    FilledTonalButton(
                                        onClick = onUnsubscribe,
                                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                                    ) {
                                        Icon(
                                            Icons.Outlined.Check,
                                            contentDescription = null,
                                            Modifier.size(ButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                        Text(stringResource(R.string.unsubscribe_from_feed))
                                    }
                                } else {
                                    Button(
                                        onClick = onSubscribe,
                                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                                    ) {
                                        Icon(
                                            Icons.Outlined.Add,
                                            contentDescription = null,
                                            Modifier.size(ButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                        Text(stringResource(R.string.subscribe_to_feed))
                                    }
                                }
                            }

                            IconButton(onClick = {}) { // TODO: Launch browser.
                                Icon(
                                    Icons.Outlined.Public,
                                    contentDescription = stringResource(R.string.feed_website)
                                )
                            }

                            IconButton(onClick = {}) { // TODO: Launch menu.
                                Icon(
                                    Icons.Outlined.MoreHoriz,
                                    contentDescription = stringResource(R.string.navigate_more)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            uiState.feed.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp, 16.dp, 16.dp)
                        )
                    }

                    item {
                        TopAppBar(
                            title = {
                                Text(
                                    pluralStringResource(
                                        R.plurals.number_of_episodes,
                                        uiState.feed.episodeCount, uiState.feed.episodeCount
                                    ),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            actions = {
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Sort,
                                        contentDescription = stringResource(R.string.sort)
                                    )
                                }
                            }
                        )
                        HorizontalDivider()
                    }

                    items(
                        uiState.episodes,
                        key = { it.id }
                    ) { episode ->
                        val publishedAt by remember(episode.published) {
                            derivedStateOf {
                                val formatter =
                                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                episode.published.format(formatter)
                            }
                        }

                        val isPlaying by remember(player.state, episode.id) {
                            derivedStateOf {
                                if (player.state is PlayerState.Active) {
                                    return@derivedStateOf player.state.currentEpisode.id == episode.id
                                }
                                false
                            }
                        }

                        EpisodeListItem(
                            publishedDateContent = { Text(publishedAt) },
                            titleContent = { Text(episode.title) },
                            descriptionContent = { Text(episode.description, maxLines = 2) }
                        ) {
                            AssistChip(
                                onClick = { player.playEpisode(episode.id) },
                                label = {
                                    if (isPlaying) {
                                        require(player.state is PlayerState.Active)

                                        Text(
                                            stringResource(
                                                R.string.duration_left,
                                                player.state.timeLeft.format(DurationFormatter.SHORT)
                                            )
                                        )
                                    } else {
                                        Text(episode.duration.format(DurationFormatter.SHORT))
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.PlayCircleOutline,
                                        contentDescription = null // TODO: Add localized description.
                                    )
                                },
                                enabled = !isPlaying
                            )

                            Spacer(Modifier.width(8.dp))

                            IconButton(
                                onClick = { player.addEpisodeToQueue(episode.id) },
                                enabled = episode.id > 0
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.PlaylistAdd,
                                    contentDescription = null
                                )
                            }

                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.DownloadForOffline, contentDescription = null)
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun Pane(
    modifier: Modifier = Modifier,
    paneModifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.padding(bottom = playerInsetHeight(8.dp))) {
        Surface(
            modifier = paneModifier.clip(MaterialTheme.shapes.extraLarge),
            color = MaterialTheme.colorScheme.surface,
            content = content
        )
    }
}

class FeedUiStatePreviewParameterProvider : PreviewParameterProvider<FeedUiState> {
    override val values = sequenceOf(
        FeedUiState.HasFeed(
            feed = Feed(
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
            episodes = listOf(
                Episode(
                    id = 1,
                    title = "Wedding Planer",
                    description = "Et vaskeægte r8Dio bryllup står snart for døren og den allestedsnærværende programchef Allan Sindberg har taget projektlederrollen i den forbindelse. Som bekendt er der dog allerede rigeligt på Smørstemmen tallerken, så han har sammen med Randi hyret en såkaldt Wedding Planer. Direktør Klavs Bundgaard har møder på tapetet. Ikke mindst med advokat Henrik Bruun, som skal hjælpe med at få styr på de fake annoncer med nyhedschef Rasmus Bruun i hovedrollen, som huserer på diverse social media platforme. </p>\n<p>Programmet er lavet i samarbejde med Meny.</p><p>Bliv medlem: ht...",
                    published = LocalDateTime.ofEpochSecond(1714104000, 0, ZoneOffset.UTC),
                    source = "https://traffic.omny.fm/d/clips/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/abee6fed-505b-457e-89c7-b15601602019/audio.mp3?utm_source=Podcast&in_playlist=6af053db-da47-47dc-aee5-abca00c013d7",
                    sourceType = "audio/mpeg",
                    sourceLength = 57624486,
                    duration = 3599.seconds,
                    isExplicit = false,
                    episode = 212,
                    season = 8,
                    image = "https://www.omnycontent.com/d/playlist/504fd940-e457-44cf-9019-abca00be97ea/aa9b1acb-fc7d-48f6-b067-abca00beaa16/6af053db-da47-47dc-aee5-abca00c013d7/image.jpg?t=1591603098&size=Large",
                    feedId = 1262243
                ),
                Episode(
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
            lastListened = ProgressWithEpisode(
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
            id = 1,
            isLoading = false,
            messages = emptyList()
        ),
        FeedUiState.NoFeed(
            id = 2,
            isLoading = false,
            messages = emptyList()
        )
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FeedScreenPreview(
    @PreviewParameter(FeedUiStatePreviewParameterProvider::class) uiState: FeedUiState
) {
    PodcastsTheme {
        CompactFeedScreen(uiState, {}, {}, {})
    }
}