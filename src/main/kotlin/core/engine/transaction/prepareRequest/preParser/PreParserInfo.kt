package core.engine.transaction.prepareRequest.preParser

import core.engine.FinalizeRequestTransaction
import core.engine.PrepareTransaction
import core.engine.Request
import core.engine.transaction.PageCondition

interface PreParserInfo<Document : Request> {
    val passCondition : PageCondition<PrepareTransaction<Document>, Document>

    val retryCondition : PageCondition<FinalizeRequestTransaction<Document>, Document>
}