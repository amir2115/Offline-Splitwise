package com.encer.offlinesplitwise.data.preferences

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val name: String,
    val username: String,
)
