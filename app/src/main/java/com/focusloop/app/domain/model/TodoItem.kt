package com.focusloop.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val text: String,
    val completed: Boolean = false
)
