package com.encer.offlinesplitwise.domain.usecase.base

abstract class BaseUseCase<in Params, out Result> {
    abstract operator fun invoke(params: Params): Result
}
