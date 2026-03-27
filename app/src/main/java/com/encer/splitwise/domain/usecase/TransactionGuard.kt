package com.encer.splitwise.domain.usecase

fun canCreateTransaction(memberCount: Int, isEdit: Boolean): Boolean = isEdit || memberCount >= 2
