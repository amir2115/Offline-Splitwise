package com.encer.splitwise.data.remote.network

import java.time.Instant

fun parseIsoInstant(value: String): Long = Instant.parse(value).toEpochMilli()

fun formatIsoInstant(value: Long): String = Instant.ofEpochMilli(value).toString()
