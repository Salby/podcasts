package me.salby.podcasts.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explicit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.salby.podcasts.R
import me.salby.podcasts.data.podcasts.model.Feed
import me.salby.podcasts.ui.theme.PodcastsTheme

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FeedsGrid(
    feeds: List<Feed>,
    modifier: Modifier = Modifier,
    numberOfColumns: Int = 2,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: ((Feed) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(numberOfColumns),
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding
    ) {
        items(feeds, key = { "feed-${it.id}-${it.podcastIndexOrgId}" }) { feed ->
            Surface(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick != null) { onClick?.invoke(feed) },
                shape = MaterialTheme.shapes.small,
                color = Color.Transparent
            ) {
                Column {
                    BoxWithConstraints {
                        GlideImage(
                            model = feed.image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(maxWidth)
                                .clip(MaterialTheme.shapes.small),
                            loading = placeholder(ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh)),
                            failure = placeholder(ColorPainter(MaterialTheme.colorScheme.secondaryContainer)),
                            transition = CrossFade
                        )
                    }

                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            feed.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (feed.isExplicit) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.Explicit,
                                contentDescription = stringResource(R.string.feed_explicit),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FeedsGridPreview() {
    PodcastsTheme {
        Surface {
            FeedsGrid(
                feeds = List(4) {
                    Feed(
                        id = it,
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
                    )
                },
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}