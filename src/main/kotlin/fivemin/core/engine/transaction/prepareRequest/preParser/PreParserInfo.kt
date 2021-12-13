package fivemin.core.engine.transaction.prepareRequest.preParser

import fivemin.core.engine.FinalizeRequestTransaction
import fivemin.core.engine.PrepareTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.transaction.PageCondition

interface PreParserInfo<Document : Request> {
    val passCondition : PageCondition<PrepareTransaction<Document>, Document>

    val retryCondition : PageCondition<FinalizeRequestTransaction<Document>, Document>
}