package core.engine.transaction.prepareRequest.preParser

import arrow.core.*
import core.engine.*
import core.engine.transaction.PageCondition
import core.engine.transaction.PrepareRequestTransactionImpl
import core.engine.transaction.prepareRequest.PreParser

class PreParserImpl(
    private val globalCondition: PageCondition<InitialTransaction<Request>, Request>,
    private val pages: List<PreParserPage>,
    private val attributeRequestOption: RequestOption
) : PreParser {

    override fun <Document : Request> generateInfo(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        var ret = globalCondition.check(init).isMet

        return if(ret && init.request.requestType == RequestType.LINK){
            return pages.map{
                it.makeTransaction(init)
            }.filterOption().singleOrNone()
        } else if(ret){
            Some(PrepareRequestTransactionImpl(init, init.tags, attributeRequestOption))
        }
        else{
            none()
        }
    }

}