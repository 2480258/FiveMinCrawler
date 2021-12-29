package com.fivemin.core.engine.transaction.prepareRequest

import arrow.core.Option
import com.fivemin.core.engine.InitialTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request

interface PreParser {
    fun <Document : Request>generateInfo(init : InitialTransaction<Document>) : Option<PrepareTransaction<Document>>
}