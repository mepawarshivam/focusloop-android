package com.focusloop.app.data.repository

import com.focusloop.app.domain.model.RecommendationItem
import com.focusloop.app.domain.model.RecommendationType
import java.net.URLEncoder

private const val GENERAL_TAG = "General Focus"

/**
 * Fully offline, hand-curated recommendation catalog. Nothing here is fetched
 * over the network — each item's [RecommendationItem.url] is a search deep
 * link built at read time, so tapping an item always lands on a live,
 * relevant page without this app ever calling out to a content API.
 */
class RecommendationRepository {

    fun getRecommendations(
        hobbies: Set<String>,
        goalTitle: String,
        type: RecommendationType,
        todoTexts: List<String> = emptyList()
    ): List<RecommendationItem> {
        val matchedTags = matchTags(hobbies, goalTitle, todoTexts)
        val pool = catalog.filter { it.type == type }
        val personalized = pool.filter { it.tag in matchedTags }
        val fallback = pool.filter { it.tag == GENERAL_TAG }
        val result = (personalized + fallback).distinctBy { it.id }
        return if (result.isEmpty()) pool.take(4) else result.take(6)
    }

    private fun matchTags(hobbies: Set<String>, goalTitle: String, todoTexts: List<String>): Set<String> {
        val fromHobbies = hobbies
        val lowerText = (listOf(goalTitle) + todoTexts).joinToString(" ").lowercase()
        val fromText = keywordToTag.filterKeys { lowerText.contains(it) }.values
        return (fromHobbies + fromText + GENERAL_TAG).toSet()
    }

    private val keywordToTag = mapOf(
        "code" to "Coding & Tech", "system design" to "Coding & Tech", "program" to "Coding & Tech",
        "study" to "Reading", "read" to "Reading", "book" to "Reading",
        "workout" to "Fitness & Wellness", "gym" to "Fitness & Wellness", "run" to "Fitness & Wellness",
        "music" to "Music", "guitar" to "Music", "piano" to "Music",
        "design" to "Art & Design", "draw" to "Art & Design", "art" to "Art & Design",
        "cook" to "Cooking", "recipe" to "Cooking", "meal" to "Cooking",
        "meditat" to "Mindfulness", "journal" to "Mindfulness", "sleep" to "Mindfulness",
        "hike" to "Outdoors & Nature", "outdoor" to "Outdoors & Nature", "garden" to "Outdoors & Nature",
        "game" to "Gaming",
        "career" to "Business & Career", "resume" to "Business & Career", "interview" to "Business & Career",
        "work" to "Business & Career"
    )

    private fun yt(query: String) =
        "https://www.youtube.com/results?search_query=${URLEncoder.encode(query, "UTF-8")}"

    private fun web(query: String) =
        "https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}"

    private fun meetup(query: String) =
        "https://www.meetup.com/find/?keywords=${URLEncoder.encode(query, "UTF-8")}"

    private val catalog: List<RecommendationItem> by lazy { buildCatalog() }

    private fun buildCatalog(): List<RecommendationItem> {
        val items = mutableListOf<RecommendationItem>()
        var counter = 0
        fun next() = "rec_${counter++}"

        fun video(tag: String, title: String, source: String) =
            items.add(RecommendationItem(next(), RecommendationType.VIDEO, title, source, tag, yt(title)))
        fun article(tag: String, title: String, source: String) =
            items.add(RecommendationItem(next(), RecommendationType.ARTICLE, title, source, tag, web(title)))
        fun event(tag: String, title: String, source: String) =
            items.add(RecommendationItem(next(), RecommendationType.EVENT, title, source, tag, meetup(title)))

        // Coding & Tech
        video("Coding & Tech", "System design interview basics in 10 minutes", "YouTube search")
        video("Coding & Tech", "Clean code habits every developer should know", "YouTube search")
        article("Coding & Tech", "How senior engineers approach system design", "Web search")
        article("Coding & Tech", "The pragmatic programmer's guide to deep work", "Web search")
        event("Coding & Tech", "local tech meetups and coding groups", "Meetup search")

        // Reading
        video("Reading", "How to actually remember what you read", "YouTube search")
        video("Reading", "Speed reading techniques that work", "YouTube search")
        article("Reading", "best books to build better habits", "Web search")
        article("Reading", "how to build a daily reading habit", "Web search")
        event("Reading", "local book clubs near me", "Meetup search")

        // Fitness & Wellness
        video("Fitness & Wellness", "10 minute full body workout no equipment", "YouTube search")
        video("Fitness & Wellness", "beginner mobility and stretching routine", "YouTube search")
        article("Fitness & Wellness", "science backed benefits of short workouts", "Web search")
        article("Fitness & Wellness", "how to build a sustainable fitness habit", "Web search")
        event("Fitness & Wellness", "local running and fitness groups", "Meetup search")

        // Music
        video("Music", "learn your first guitar chords in 10 minutes", "YouTube search")
        video("Music", "music theory basics for beginners", "YouTube search")
        article("Music", "how learning an instrument changes your brain", "Web search")
        article("Music", "best apps to practice music daily", "Web search")
        event("Music", "local live music and jam sessions", "Meetup search")

        // Art & Design
        video("Art & Design", "beginner sketching techniques in 10 minutes", "YouTube search")
        video("Art & Design", "principles of good visual design explained", "YouTube search")
        article("Art & Design", "how to build a daily sketching habit", "Web search")
        article("Art & Design", "design inspiration for beginners", "Web search")
        event("Art & Design", "local art classes and design meetups", "Meetup search")

        // Cooking
        video("Cooking", "5 quick healthy recipes anyone can cook", "YouTube search")
        video("Cooking", "basic knife skills every cook should know", "YouTube search")
        article("Cooking", "easy meal prep ideas for the week", "Web search")
        article("Cooking", "how cooking at home improves wellbeing", "Web search")
        event("Cooking", "local cooking classes near me", "Meetup search")

        // Mindfulness
        video("Mindfulness", "5 minute guided breathing exercise", "YouTube search")
        video("Mindfulness", "how to start a meditation practice", "YouTube search")
        article("Mindfulness", "the science of mindfulness and focus", "Web search")
        article("Mindfulness", "simple journaling prompts for clarity", "Web search")
        event("Mindfulness", "local meditation and mindfulness groups", "Meetup search")

        // Outdoors & Nature
        video("Outdoors & Nature", "best beginner hiking tips", "YouTube search")
        video("Outdoors & Nature", "how to start a small home garden", "YouTube search")
        article("Outdoors & Nature", "benefits of spending time outdoors", "Web search")
        article("Outdoors & Nature", "beginner friendly hiking trails guide", "Web search")
        event("Outdoors & Nature", "local hiking and outdoors groups", "Meetup search")

        // Gaming
        video("Gaming", "game design basics for beginners", "YouTube search")
        video("Gaming", "speedrunning techniques explained", "YouTube search")
        article("Gaming", "how game design uses psychology", "Web search")
        article("Gaming", "best indie games to try this year", "Web search")
        event("Gaming", "local gaming meetups and tournaments", "Meetup search")

        // Business & Career
        video("Business & Career", "how to negotiate your salary confidently", "YouTube search")
        video("Business & Career", "resume tips that actually get interviews", "YouTube search")
        article("Business & Career", "how to build a personal career roadmap", "Web search")
        article("Business & Career", "productivity habits of top performers", "Web search")
        event("Business & Career", "local networking and career events", "Meetup search")

        // General Focus (always-available fallback)
        video(GENERAL_TAG, "why we get distracted and how to fix it", "YouTube search")
        video(GENERAL_TAG, "the pomodoro technique explained in 5 minutes", "YouTube search")
        article(GENERAL_TAG, "science backed ways to beat procrastination", "Web search")
        article(GENERAL_TAG, "how to build habits that actually stick", "Web search")
        event(GENERAL_TAG, "local focus and productivity meetups", "Meetup search")

        return items
    }
}
