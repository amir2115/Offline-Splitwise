package com.encer.splitwise.data.local.db

import androidx.room.TypeConverter
import com.encer.splitwise.data.local.entity.SyncState
import com.encer.splitwise.domain.model.MembershipStatus
import com.encer.splitwise.domain.model.SplitType

class Converters {
    @TypeConverter
    fun splitTypeToString(splitType: SplitType): String = splitType.name

    @TypeConverter
    fun stringToSplitType(value: String): SplitType = SplitType.valueOf(value)

    @TypeConverter
    fun syncStateToString(syncState: SyncState): String = syncState.name

    @TypeConverter
    fun stringToSyncState(value: String): SyncState = SyncState.valueOf(value)

    @TypeConverter
    fun membershipStatusToString(status: MembershipStatus): String = status.name

    @TypeConverter
    fun stringToMembershipStatus(value: String): MembershipStatus = MembershipStatus.valueOf(value)
}
