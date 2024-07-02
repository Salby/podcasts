package me.salby.podcasts

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import me.salby.podcasts.navigation.PodcastsNavHost
import me.salby.podcasts.ui.theme.PodcastsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.salby.podcasts.data.player.PlayerRepository
import me.salby.podcasts.ui.Player
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ProvidePlayerState(
                playerRepository,
                { playerRepository.play() },
                { episodeId, positionMs -> playerRepository.playEpisode(episodeId, positionMs) },
                onPause = { playerRepository.pause() },
                onSeekNext = { playerRepository.seekNext() },
                onSeekPrevious = { playerRepository.seekPrevious()},
                onSetPosition = { playerRepository.setPosition(it) },
                onPlayEpisode = { playerRepository.playEpisode(it) },
                onAddEpisodeToQueue = { playerRepository.addEpisodeToQueue(it) }
            ) {
                PodcastsTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        PodcastsNavHost(modifier = Modifier.fillMaxSize())

                        Player(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}