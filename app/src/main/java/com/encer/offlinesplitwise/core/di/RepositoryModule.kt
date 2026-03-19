package com.encer.offlinesplitwise.core.di

import com.encer.offlinesplitwise.data.repository.DefaultExpenseRepository
import com.encer.offlinesplitwise.data.repository.DefaultGroupRepository
import com.encer.offlinesplitwise.data.repository.DefaultMemberRepository
import com.encer.offlinesplitwise.data.repository.DefaultSettlementRepository
import com.encer.offlinesplitwise.domain.repository.ExpenseRepository
import com.encer.offlinesplitwise.domain.repository.GroupRepository
import com.encer.offlinesplitwise.domain.repository.MemberRepository
import com.encer.offlinesplitwise.domain.repository.SettlementRepository
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
