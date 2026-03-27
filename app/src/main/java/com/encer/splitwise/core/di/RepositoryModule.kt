package com.encer.splitwise.core.di

import com.encer.splitwise.data.repository.DefaultExpenseRepository
import com.encer.splitwise.data.repository.DefaultGroupRepository
import com.encer.splitwise.data.repository.DefaultMemberRepository
import com.encer.splitwise.data.repository.DefaultSettlementRepository
import com.encer.splitwise.domain.repository.ExpenseRepository
import com.encer.splitwise.domain.repository.GroupRepository
import com.encer.splitwise.domain.repository.MemberRepository
import com.encer.splitwise.domain.repository.SettlementRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindGroupRepository(impl: DefaultGroupRepository): GroupRepository
    @Binds @Singleton abstract fun bindMemberRepository(impl: DefaultMemberRepository): MemberRepository
    @Binds @Singleton abstract fun bindExpenseRepository(impl: DefaultExpenseRepository): ExpenseRepository
    @Binds @Singleton abstract fun bindSettlementRepository(impl: DefaultSettlementRepository): SettlementRepository
}
