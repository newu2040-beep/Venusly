package com.example.model

enum class Gender(val displayName: String) {
    FEMALE("Female"),
    MALE("Male"),
    NON_BINARY("Non-Binary"),
    PREFER_NOT_TO_SAY("Prefer Not to Say")
}

data class UserProfile(
    val name: String = "Aesthetic Creator",
    val bio: String = "Pastel photography lover & visual storyteller ✨",
    val avatarUri: String? = null,
    val age: Int = 22,
    val gender: Gender = Gender.FEMALE,
    val instagramHandle: String = "@venusly_creator",
    val location: String = "Tokyo / Seoul / Paris",
    val photographyStyle: String = "Pastel & Analog Minimalist"
)
