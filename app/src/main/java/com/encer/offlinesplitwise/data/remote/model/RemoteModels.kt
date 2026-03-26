package com.encer.offlinesplitwise.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiUser(
    val id: String,
    val name: String,
    val username: String,
)

@Serializable
data class TokenPair(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
)

@Serializable
data class AuthResponse(
    val user: ApiUser,
    val tokens: TokenPair,
)

@Serializable
data class AuthRegisterRequest(
    val name: String,
    val username: String,
    val password: String,
)

@Serializable
data class AuthLoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class TokenRefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class HealthCheckResponse(
    val status: String? = null,
    @SerialName("min_supported_version_code") val minSupportedVersionCode: Int? = null,
    @SerialName("latest_version_code") val latestVersionCode: Int? = null,
    @SerialName("update_mode") val updateMode: String? = null,
    @SerialName("store_url") val storeUrl: String? = null,
    @SerialName("update_title") val updateTitle: String? = null,
    @SerialName("update_message") val updateMessage: String? = null,
)

@Serializable
data class RemoteExpenseParticipantAmount(
    @SerialName("member_id") val memberId: String,
    val amount: Int,
)

@Serializable
data class RemoteGroup(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("user_id") val userId: String,
)

@Serializable
data class RemoteMember(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val username: String,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("membership_status") val membershipStatus: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("user_id") val userId: String? = null,
)

@Serializable
data class RemoteExpense(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val title: String,
    val note: String? = null,
    @SerialName("total_amount") val totalAmount: Int,
    @SerialName("split_type") val splitType: String,
    val payers: List<RemoteExpenseParticipantAmount>,
    val shares: List<RemoteExpenseParticipantAmount>,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("user_id") val userId: String,
)

@Serializable
data class RemoteSettlement(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("from_member_id") val fromMemberId: String,
    @SerialName("to_member_id") val toMemberId: String,
    val amount: Int,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("user_id") val userId: String,
)

@Serializable
data class RemoteSyncChanges(
    val groups: List<RemoteGroup> = emptyList(),
    val members: List<RemoteMember> = emptyList(),
    val expenses: List<RemoteExpense> = emptyList(),
    val settlements: List<RemoteSettlement> = emptyList(),
    @SerialName("deleted_group_ids") val deletedGroupIds: List<String> = emptyList(),
    @SerialName("deleted_member_ids") val deletedMemberIds: List<String> = emptyList(),
    @SerialName("deleted_expense_ids") val deletedExpenseIds: List<String> = emptyList(),
    @SerialName("deleted_settlement_ids") val deletedSettlementIds: List<String> = emptyList(),
)

@Serializable
data class SyncResponse(
    @SerialName("server_time") val serverTime: String,
    @SerialName("next_cursor") val nextCursor: String,
    val changes: RemoteSyncChanges,
)

@Serializable
data class RemoteGroupPayload(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class RemoteMemberPayload(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val username: String,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class RemoteGroupInvite(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("member_id") val memberId: String,
    val username: String,
    @SerialName("inviter_user_id") val inviterUserId: String,
    @SerialName("invitee_user_id") val inviteeUserId: String,
    val status: String,
    @SerialName("group_name") val groupName: String,
    @SerialName("inviter_username") val inviterUsername: String,
    @SerialName("invitee_username") val inviteeUsername: String,
    @SerialName("responded_at") val respondedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class RemoteExpensePayload(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val title: String,
    val note: String? = null,
    @SerialName("total_amount") val totalAmount: Int,
    @SerialName("split_type") val splitType: String,
    val payers: List<RemoteExpenseParticipantAmount>,
    val shares: List<RemoteExpenseParticipantAmount>,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class RemoteSettlementPayload(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("from_member_id") val fromMemberId: String,
    @SerialName("to_member_id") val toMemberId: String,
    val amount: Int,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class SyncPushPayload(
    @SerialName("device_id") val deviceId: String,
    val groups: List<RemoteGroupPayload>,
    val members: List<RemoteMemberPayload>,
    val expenses: List<RemoteExpensePayload>,
    val settlements: List<RemoteSettlementPayload>,
    @SerialName("deleted_group_ids") val deletedGroupIds: List<String>,
    @SerialName("deleted_member_ids") val deletedMemberIds: List<String>,
    @SerialName("deleted_expense_ids") val deletedExpenseIds: List<String>,
    @SerialName("deleted_settlement_ids") val deletedSettlementIds: List<String>,
)

@Serializable
data class SyncRequestEnvelope(
    @SerialName("device_id") val deviceId: String,
    @SerialName("last_synced_at") val lastSyncedAt: String? = null,
    val push: SyncPushPayload? = null,
)

@Serializable
data class SyncImportRequest(
    @SerialName("device_id") val deviceId: String,
    val groups: List<RemoteGroupPayload>,
    val members: List<RemoteMemberPayload>,
    val expenses: List<RemoteExpensePayload>,
    val settlements: List<RemoteSettlementPayload>,
    @SerialName("deleted_group_ids") val deletedGroupIds: List<String>,
    @SerialName("deleted_member_ids") val deletedMemberIds: List<String>,
    @SerialName("deleted_expense_ids") val deletedExpenseIds: List<String>,
    @SerialName("deleted_settlement_ids") val deletedSettlementIds: List<String>,
)
