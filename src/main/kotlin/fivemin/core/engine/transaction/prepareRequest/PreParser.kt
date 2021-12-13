package fivemin.core.engine.transaction.prepareRequest

import arrow.core.Option
import fivemin.core.engine.InitialTransaction
import fivemin.core.engine.PrepareTransaction
import fivemin.core.engine.Request

interface PreParser {
    fun <Document : Request>generateInfo(init : InitialTransaction<Document>) : Option<PrepareTransaction<Document>>
}