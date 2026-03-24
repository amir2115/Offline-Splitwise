package com.encer.offlinesplitwise.domain.model

enum class MembershipStatus {
    ACTIVE,
    PENDING_INVITE,
}

data class Member(
    val id: String,
    val groupId: String,
    val username: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val userId: String? = null,
    val membershipStatus: MembershipStatus = MembershipStatus.ACTIVE,
)

fun memberName(members: List<Member>, memberId: String): String {
    return members.firstOrNull { it.id == memberId }?.username ?: if (java.util.Locale.getDefault().language == "fa") "کاربر $memberId" else "Member $memberId"
}
