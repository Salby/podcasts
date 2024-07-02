package me.salby.podcasts.ui.episode

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.rounded.DownloadForOffline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.salby.podcasts.ui.DurationFormatter
import me.salby.podcasts.ui.format
import me.salby.podcasts.ui.theme.PodcastsTheme
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun EpisodeListItem(
    publishedDateContent: @Composable () -> Unit,
    titleContent: @Composable () -> Unit,
    descriptionContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    imageContent: (@Composable () -> Unit)? = null,
    feedTitleContent: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                imageContent?.let {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        it()
                    }
                }
                Column(modifier = Modifier.padding(end = 16.dp)) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleSmall) {
                        feedTitleContent?.invoke()
                    }
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                        LocalTextStyle provides MaterialTheme.typography.labelMedium
                    ) {
                        Row {
                            publishedDateContent()
                        }
                    }
                }
            }
        }

        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                titleContent()
            }
        }
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            LocalTextStyle provides MaterialTheme.typography.bodySmall
        ) {
            Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp)) {
                descriptionContent()
            }
        }

        actions?.let {
            Row(
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 4.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                content = it
            )
        } ?: Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EpisodeListItemPreview() {
    PodcastsTheme {
        val date = LocalDateTime.ofEpochSecond(1713499200, 0, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        val formatted = date.format(formatter)

        Surface {
            EpisodeListItem(
                feedTitleContent = { Text("Undskyld vi roder") },
                publishedDateContent = { Text(formatted) },
                titleContent = { Text("Russere og den lille r8Diomus") },
                descriptionContent = {
                    Text(
                        "Afsnit 211. Klavs og Emil skal holde et skæbnesvangert møde med en udenlandsk interessent, men inden da ringer Klavs til Mickey Meny for at beklage en lille hændelse under lanceringsfesten for den nye øl, Bøvøl. Alle har ellers været enige om, at det var en fantastisk begivenhed og genialt af Mickey at mixe en hundeudstilling, musik og Bøvøl i måske én af de største Meny’er i verden. Herefter giver Rasmus Bruun en status. Han er efterhånden ved at være tilbage i topform. Dog viser det sig senere, at t...",
                        maxLines = 2
                    )
                },
                actions = {
                    AssistChip(
                        onClick = { /*TODO*/ },
                        label = { Text(2935.seconds.format(DurationFormatter.SHORT)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.PlayCircleOutline,
                                contentDescription = null
                            )
                        }
                    )

                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Outlined.PlaylistAdd, contentDescription = null)
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.DownloadForOffline, contentDescription = null)
                    }
                }
            )
        }
    }
}