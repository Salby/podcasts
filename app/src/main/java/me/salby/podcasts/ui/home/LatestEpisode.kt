package me.salby.podcasts.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun LatestEpisode(
    model: ProgressWithEpisode,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeLeft by remember(model.episode.duration, model.progress.progress) {
        derivedStateOf {
            val milliseconds = model.episode.duration.inWholeMilliseconds - model.progress.progress
            milliseconds.milliseconds
        }
    }

    val formattedDate by remember(model.episode.published) {
        derivedStateOf {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
            model.episode.published.format(formatter)
        }
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Text(
            stringResource(R.string.continue_listening),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp)
        )
        EpisodeListItem(
            publishedDateContent = {
                Text(formattedDate)
            },
            titleContent = {
                Text(model.episode.title)
            },
            descriptionContent = {
                Text(
                    model.episode.description,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
            },
            actions = {
                Button(
                    onClick = onPlay,
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
fun LatestEpisodePreview() {
    PodcastsTheme {
        LatestEpisode(
            model = ProgressWithEpisode(
                progress = Progress(
                    id = 1,
                    progress = 1782.seconds.inWholeMilliseconds,
                    LocalDateTime.now(), LocalDateTime.now(), 2
                ),
                episode = Episode(
                    id = 2108161,
                    title = "High and Low (1963)",
                    description = "“Success isn't worth losing your humanity.\"\n60s MONTH continues with HIGH AND LOW. Danny has launched a brand new podcast about theme parks The Yeti is Still Broken, perhaps the first one in history, Proto watches GONE GIRL, we announce our Plan B options for the final week of this month that Patrons will be voting on, JUMANJI, Robert Redford's hair in THREE DAYS OF THE CONDOR, and much more. The uncut episode just for Patrons includes an extra 40 minutes of stuff the new Disney Plus logo, Danny going on a Hitchcock journey, WrestleMania, DANTE's PEAK, Coppola an...",
                    published = LocalDateTime.ofEpochSecond(1712552400, 0, ZoneOffset.UTC),
                    source = "https://anchor.fm/s/12d1fabc/podcast/play/85089465/https%3A%2F%2Fd3ctxlq1ktw2nl.cloudfront.net%2Fstaging%2F2024-3-7%2Fd9cc892e-fe82-ff3a-133d-6ff44af4b55a.mp3",
                    sourceType = "audio/mpeg",
                    sourceLength = 151515897,
                    duration = 4628.seconds,
                    isExplicit = true,
                    episode = 213,
                    season = 0,
                    image = "https://d3t3ozftmdmh3i.cloudfront.net/staging/podcast_uploaded_episode/3057511/3057511-1712504111360-26bf213677d73.jpg",
                    feedId = 1262243
                )
            ),
            onPlay = {}
        )
    }
}