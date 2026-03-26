package com.encer.offlinesplitwise.domain.usecase

fun canCreateTransaction(memberCount: Int, isEdit: Boolean): Boolean = isEdit || memberCount >= 2
