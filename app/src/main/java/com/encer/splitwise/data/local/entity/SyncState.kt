package com.encer.splitwise.data.local.entity

enum class SyncState {
    SYNCED,
    PENDING_UPSERT,
    PENDING_DELETE,
}
