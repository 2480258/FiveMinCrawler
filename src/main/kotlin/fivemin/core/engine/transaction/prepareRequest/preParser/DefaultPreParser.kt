package fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import fivemin.core.engine.*
import fivemin.core.engine.transaction.PageCondition
import fivemin.core.engine.transaction.PrepareRequestTransactionImpl
import fivemin.core.engine.transaction.prepareRequest.PreParser

class DefaultPreParser(
    private val globalCondition: PageCondition<InitialTransaction<Request>, Request>,
    private val pages: List<PreParserPage>,
    private val attributeRequestOption: RequestOption
) : PreParser {
    override fun <Document : Request> generateInfo(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        var ret = globalCondition.check(init).isMet

        if (ret && init.request.requestType == RequestType.LINK) {
            return pages.map {
                it.makeTransaction(init)
            }.single {
                it.isNotEmpty()
            }
        } else if (ret) {
            return PrepareRequestTransactionImpl<Document>(init, init.tags, attributeRequestOption).toOption()
        } else {
            return none()
        }
    }
}