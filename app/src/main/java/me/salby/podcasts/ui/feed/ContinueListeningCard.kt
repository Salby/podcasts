package me.salby.podcasts.ui.feed

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.salby.podcasts.R
import me.salby.podcasts.data.podcasts.model.Episode
import me.salby.podcasts.data.podcasts.model.Progress
import me.salby.podcasts.data.podcasts.model.ProgressWithEpisode
import me.salby.podcasts.ui.DurationFormatter
import me.salby.podcasts.ui.episode.EpisodeListItem
import me.salby.podcasts.ui.format
import me.salby.podcasts.ui.theme.PodcastsTheme
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun ContinueListeningCard(
    model: ProgressWithEpisode,
    onPlayEpisode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeLeft by remember(model.episode.duration, model.progress.progress) {
        derivedStateOf {
            val milliseconds = model.episode.duration.inWholeMilliseconds - model.progress.progress
            milliseconds.milliseconds
        }
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        EpisodeListItem(
            publishedDateContent = {
                Text(
                    stringResource(R.string.continue_listening),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            },
            titleContent = {
                Text(model.episode.title)
            },
            descriptionContent = {
                Text(
                    model.episode.description,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            },
            actions = {
                Button(
                    onClick = onPlayEpisode,
                    modifier = Modifier.padding(top = 4.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(
                            R.string.duration_left,
                            timeLeft.format(DurationFormatter.SHORT)
                        )
                    )
                }
            }
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ContinueListeningCardPreview() {
    PodcastsTheme {
        ContinueListeningCard(
            model = ProgressWithEpisode(
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
            onPlayEpisode = {}
        )
    }
}