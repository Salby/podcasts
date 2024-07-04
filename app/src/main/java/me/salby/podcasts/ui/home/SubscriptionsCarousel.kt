package me.salby.podcasts.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import me.salby.podcasts.data.podcasts.model.Feed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SubscriptionsCarousel(
    feeds: List<Feed>,
    onFeedClick: (feed: Feed) -> Unit,
    modifier: Modifier = Modifier,
    carouselState: CarouselState = rememberCarouselState { feeds.size },
    preferredArtworkWidth: Dp = SubscriptionsCarouselPreferredItemSize,
    itemModifier: @Composable (Feed) -> Modifier = { Modifier }
) {
    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = preferredArtworkWidth,
        modifier = modifier,
        itemSpacing = SubscriptionsCarouselItemSpacing,
        contentPadding = PaddingValues(horizontal = SubscriptionsCarouselItemSpacing)
    ) {
        GlideImage(
            model = feeds[it].image,
            contentDescription = feeds[it].title,
            modifier = itemModifier(feeds[it])
                .size(preferredArtworkWidth)
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable { onFeedClick(feeds[it]) },
            loading = placeholder(ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh)),
            failure = placeholder(ColorPainter(MaterialTheme.colorScheme.errorContainer))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun SubscriptionsCarouselPlaceholder(
    modifier: Modifier = Modifier,
    itemModifier: () -> Modifier = { Modifier }
) {
    val carouselState = rememberCarouselState { 10 }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = SubscriptionsCarouselPreferredItemSize,
        modifier = modifier,
        itemSpacing = SubscriptionsCarouselItemSpacing,
        contentPadding = PaddingValues(horizontal = SubscriptionsCarouselItemSpacing)
    ) {
        GlideImage(
            model = null,
            contentDescription = null,
            modifier = Modifier
                .size(SubscriptionsCarouselPreferredItemSize)
                .maskClip(shape = MaterialTheme.shapes.extraLarge)
                .then(itemModifier()),
            failure = placeholder(ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh))
        )
    }
}

@Composable
fun SubscriptionsCarouselEmptyMessage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SubscriptionsCarouselPlaceholder()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SubscriptionsCarouselPreferredItemSize)
                .background(MaterialTheme.colorScheme.background.copy(alpha = .54f)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

val SubscriptionsCarouselPreferredItemSize = 120.dp
val SubscriptionsCarouselItemSpacing = 8.dp