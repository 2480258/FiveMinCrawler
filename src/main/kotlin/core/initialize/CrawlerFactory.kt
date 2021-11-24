package core.initialize

import core.engine.*
import core.engine.session.ArchivedSessionSet
import core.engine.transaction.TransactionSubPolicy
import core.engine.transaction.export.ExportParser
import core.engine.transaction.prepareRequest.PreParser
import core.engine.transaction.serialize.PostParser
import core.request.RequesterSelector

data class ResumeOption(val archivedSessionSet: ArchivedSessionSet, val continueExportStateInfo: ContinueExportStateInfo)

data class SubPolicyCollection(
    val preprocess : Iterable<TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request>>,
    val request : Iterable<TransactionSubPolicy<PrepareTransaction<Request>,FinalizeRequestTransaction<Request>, Request>>,
    val serialize : Iterable<TransactionSubPolicy<FinalizeRequestTransaction<Request>,SerializeTransaction<Request>, Request>>,
    val export : Iterable<TransactionSubPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request>>
)

data class Parseoption (val preParser : PreParser, val postParser : PostParser<Request>, val exportParser: ExportParser, val requesterSelector: RequesterSelector)

class CrawlerFactory {

}sdlk