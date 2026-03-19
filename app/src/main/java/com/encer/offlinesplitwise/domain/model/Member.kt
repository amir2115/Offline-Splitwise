package com.encer.offlinesplitwise.domain.model

data class Member(
    val id: String,
    val groupId: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val isArchived: Boolean = false,
    val userId: String? = null,
)

fun memberName(members: List<Member>, memberId: String): String {
    return members.firstOrNull { it.id == memberId }?.name ?: if (java.util.Locale.getDefault().language == "fa") "کاربر $memberId" else "Member $memberId"
}