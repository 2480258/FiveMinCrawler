package com.fivemin.core.engine.transaction

import arrow.core.toOption
import com.fivemin.core.engine.InitialTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.Transaction

interface PageCondition<in Trans : Transaction<Document>, out Document : Request> {
    fun check(trans: Trans): PageConditionResult
}

data class PageConditionResult(val isMet: Boolean)

class UriRegexPageCondition(val regex: Regex) : PageCondition<InitialTransaction<Request>, Request> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun check(trans: InitialTransaction<Request>): PageConditionResult {
        return PageConditionResult(regex.find(trans.request.target.toString()).toOption().fold({ false }, { true }))
    }
}
