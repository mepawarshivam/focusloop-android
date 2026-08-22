package com.focusloop.app.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.Book
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.Code
import compose.icons.feathericons.Coffee
import compose.icons.feathericons.Compass
import compose.icons.feathericons.Music
import compose.icons.feathericons.PenTool
import compose.icons.feathericons.PlayCircle
import compose.icons.feathericons.Wind

enum class RecommendationType { VIDEO, ARTICLE, EVENT }

/**
 * A curated, personalized suggestion shown during an intervention.
 *
 * [url] is always a search deep link (YouTube / web / Meetup search query),
 * never a single hardcoded video or article URL — that keeps every link
 * live and relevant instead of risking a stale or dead reference, while the
 * [title]/[source] pairing still reads as a specific, curated pick.
 */
data class RecommendationItem(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val source: String,
    val tag: String,
    val url: String
)

/** Curated hobby tags offered during onboarding and used to tag recommendation content. */
object HobbyTags {
    val all: List<Pair<String, ImageVector>> = listOf(
        "Fitness & Wellness" to FeatherIcons.Activity,
        "Reading" to FeatherIcons.Book,
        "Coding & Tech" to FeatherIcons.Code,
        "Music" to FeatherIcons.Music,
        "Art & Design" to FeatherIcons.PenTool,
        "Cooking" to FeatherIcons.Coffee,
        "Mindfulness" to FeatherIcons.Wind,
        "Outdoors & Nature" to FeatherIcons.Compass,
        "Gaming" to FeatherIcons.PlayCircle,
        "Business & Career" to FeatherIcons.Briefcase
    )
}
