package com.fivemin.core.engine.transaction.prepareRequest.preParser

import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.PageCondition

interface PreParserInfo<Document : Request> {
    val passCondition : PageCondition<PrepareTransaction<Document>, Document>

    val retryCondition : PageCondition<FinalizeRequestTransaction<Document>, Document>
}