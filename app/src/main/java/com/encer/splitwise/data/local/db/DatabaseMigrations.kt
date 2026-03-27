package com.encer.splitwise.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Baseline migration so upgrades from the current shipped schema stay explicit and testable.
 * Schema v4 is intentionally identical to v3; this version bump establishes a safe path
 * for future non-destructive migrations.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) = Unit
}
