package me.salby.podcasts.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import me.salby.podcasts.R
import kotlin.time.Duration

enum class DurationFormatter {
    LONG, SHORT, DIGITAL, POSITION
}

@Composable
fun Duration.format(formatter: DurationFormatter): String {
    val hours = inWholeHours.toInt()
    val minutes = (inWholeMinutes - (hours * 60)).toInt()
    val parts = mutableListOf<String>()
    val includeHours = hours > 0
    val includeMinutes = minutes > 0 || hours < 1
    return when (formatter) {
        DurationFormatter.LONG -> {
            if (includeHours) {
                parts.add(
                    pluralStringResource(
                        R.plurals.number_of_hours,
                        hours, hours
                    )
                )
            }
            if (includeMinutes) {
                parts.add(
                    pluralStringResource(
                        R.plurals.number_of_minutes,
                        minutes, minutes
                    )
                )
            }
            parts.joinToString(separator = stringResource(R.string.duration_glue))
        }

        DurationFormatter.SHORT -> {
            if (includeHours) {
                parts.add(
                    pluralStringResource(
                        R.plurals.number_of_hours_short,
                        hours, hours
                    )
                )
            }
            if (includeMinutes) {
                parts.add(
                    pluralStringResource(
                        R.plurals.number_of_minutes_short,
                        minutes, minutes
                    )
                )
            }
            parts.joinToString(separator = " ")
        }

        DurationFormatter.DIGITAL -> {
            if (includeHours) {
                parts.add("$hours".padStart(2, '0'))
            }
            if (includeMinutes) {
                parts.add("$minutes".padStart(2, '0'))
            }
            parts.joinToString(separator = ":")
        }

        DurationFormatter.POSITION -> {
            val seconds = (inWholeSeconds - (minutes * 60)).toInt()
            if (includeHours) {
                parts.add("$hours".padStart(2, '0'))
            }
            parts.add("$minutes".padStart(2, '0'))
            if (!includeHours) {
                parts.add("$seconds".padStart(2, '0'))
            }
            parts.joinToString(separator = ":")
        }
    }
}