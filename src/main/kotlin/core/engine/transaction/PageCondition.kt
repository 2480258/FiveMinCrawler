package core.engine.transaction

import arrow.core.toOption
import core.engine.InitialTransaction
import core.engine.Request
import core.engine.Transaction

interface PageCondition<in Trans : Transaction<Document>, out Document : Request> {
    fun check(trans : Trans) : PageConditionResult
}

data class PageConditionResult(val isMet : Boolean){

}

class UriRegexPageCondition(val regex: Regex) : PageCondition<InitialTransaction<Request>, Request>{
    @OptIn(ExperimentalStdlibApi::class)
    override fun check(trans: InitialTransaction<Request>): PageConditionResult {
        return PageConditionResult(regex.find(trans.request.target.toString()).toOption().fold({false}, {true}))
    }
}