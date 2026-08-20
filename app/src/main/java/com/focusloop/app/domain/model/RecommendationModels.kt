package com.focusloop.app.domain.model

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
    val all = listOf(
        "Fitness & Wellness" to "🏋️",
        "Reading" to "📚",
        "Coding & Tech" to "💻",
        "Music" to "🎵",
        "Art & Design" to "🎨",
        "Cooking" to "🍳",
        "Mindfulness" to "🧘",
        "Outdoors & Nature" to "🌲",
        "Gaming" to "🎮",
        "Business & Career" to "💼"
    )
}
