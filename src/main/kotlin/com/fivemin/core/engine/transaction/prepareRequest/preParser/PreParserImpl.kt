package com.fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageCondition
import com.fivemin.core.engine.transaction.PrepareRequestTransactionImpl
import com.fivemin.core.engine.transaction.prepareRequest.PreParser

class PreParserImpl(
    private val globalCondition: PageCondition<InitialTransaction<Request>, Request>,
    private val pages: List<PreParserPage>,
    private val attributeRequestOption: RequestOption
) : PreParser {

    companion object {
        private val logger = LoggerController.getLogger("PreParserImpl")
    }

    override fun <Document : Request> generateInfo(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        var ret = globalCondition.check(init).isMet

        return if (ret && init.request.requestType == RequestType.LINK) {
            var ret = pages.map {
                Pair(it, it.makeTransaction(init))
            }

            logPages(ret, init)

            ret.map {
                it.second
            }.filterOption().singleOrNone()
        } else if (ret) {
            Some(PrepareRequestTransactionImpl(init, init.tags, attributeRequestOption))
        } else {
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

        if (pages.count() > 1) {
            pages.map {
                logger.warn(
                    init.request, "has conflicting pages: " + ret.map {
                        it.first
                    }.fold("") { x, y -> x + ", " + y.name.name }
                )
            }
        }

        if (!pages.any()) {
            logger.warn(init.request, "has no matching pages")
        }
    }
}
