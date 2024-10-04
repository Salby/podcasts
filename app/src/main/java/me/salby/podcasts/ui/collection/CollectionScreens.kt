package me.salby.podcasts.ui.collection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.salby.podcasts.R
import me.salby.podcasts.data.PreviewFeeds
import me.salby.podcasts.ui.theme.PodcastsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactCollectionScreen(
    uiState: CollectionUiState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.collection))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentPadding = innerPadding
        ) {

        }
    }
}

class CollectionUiStatePreviewParameterProvider : PreviewParameterProvider<CollectionUiState> {
    override val values = sequenceOf(
        CollectionUiState.HasFeeds(
            feeds = PreviewFeeds,
            isLoading = false
        ),
        CollectionUiState.NoFeeds(
            isLoading = false
        )
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CollectionScreenPreview(
    @PreviewParameter(CollectionUiStatePreviewParameterProvider::class) uiState: CollectionUiState
) {
    PodcastsTheme {
        CompactCollectionScreen(uiState)
    }
}