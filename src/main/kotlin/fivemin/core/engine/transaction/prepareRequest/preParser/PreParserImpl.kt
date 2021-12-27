package fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.*
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.PageCondition
import fivemin.core.engine.transaction.PrepareRequestTransactionImpl
import fivemin.core.engine.transaction.prepareRequest.PreParser

class PreParserImpl(
    private val globalCondition: PageCondition<InitialTransaction<Request>, Request>,
    private val pages: List<PreParserPage>,
    private val attributeRequestOption: RequestOption
) : PreParser {
    
    companion object {
        private val logger = LoggerController.getLogger("SerializeTransactionMovementImpl")
    }
    
    override fun <Document : Request> generateInfo(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        var ret = globalCondition.check(init).isMet

        return if(ret && init.request.requestType == RequestType.LINK){
            var ret = pages.map{
                Pair(it, it.makeTransaction(init))
            }
    
            logPages(ret, init)
            
            ret.map {
                it.second
            }.filterOption().singleOrNone()
        } else if(ret){
            Some(PrepareRequestTransactionImpl(init, init.tags, attributeRequestOption))
        }
        else{
            none()
        }
    }
    
    private fun <Document : Request> logPages(
        ret: List<Pair<PreParserPage, Option<PrepareTransaction<Document>>>>,
        init: InitialTransaction<Document>
    ) {
        val pages = ret.filter {
            it.second.isNotEmpty()
        }
        
        if(pages.count() > 1) {
            pages.map {
                logger.warn(init.request.getDebugInfo() + " < has conflicting pages: " + ret.map {
                    it.first
                }.fold("") { x, y -> x + ", " + y.name.name })
            }
        }
        
        if(!pages.any()) {
            logger.warn(init.request.getDebugInfo() + " < has no matching pages")
        }
    }
    
}