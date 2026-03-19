package com.encer.offlinesplitwise.data.local.db

import androidx.room.TypeConverter
import com.encer.offlinesplitwise.data.local.entity.SyncState
import com.encer.offlinesplitwise.domain.model.SplitType

class Converters {
    @TypeConverter
    fun splitTypeToString(splitType: SplitType): String = splitType.name

    @TypeConverter
    fun stringToSplitType(value: String): SplitType = SplitType.valueOf(value)

    @TypeConverter
    fun syncStateToString(syncState: SyncState): String = syncState.name

    @TypeConverter
    fun stringToSyncState(value: String): SyncState = SyncState.valueOf(value)
}
