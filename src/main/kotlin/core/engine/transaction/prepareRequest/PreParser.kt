package core.engine.transaction.prepareRequest

import arrow.core.Option
import core.engine.InitialTransaction
import core.engine.PrepareTransaction
import core.engine.Request

interface PreParser {
    fun <Document : Request>generateInfo(init : InitialTransaction<Document>) : Option<PrepareTransaction<Document>>
}