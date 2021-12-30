package com.fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageCondition
import com.fivemin.core.engine.transaction.PrepareDocumentRequestTransactionImpl
import com.fivemin.core.engine.transaction.TagBuilder

interface PreParserPage {
    val name: PageName
    fun <Document : Request> makeTransaction(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>>
}

class PreParserPageImpl(
    override val name: PageName,
    private val condition: PageCondition<InitialTransaction<Request>, Request>,
    private val containerOption: ContainerOption,
    private val requestOption: RequestOption,
    private val tagBuilder: TagBuilder<InitialTransaction<Request>, Request>
) : PreParserPage {

    companion object {
        private val logger = LoggerController.getLogger("PostParserContentPageImpl")
    }

    override fun <Document : Request> makeTransaction(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        var ret = if (isPageNamePreDefined(init)) {
            checkPreDefined(init)
        } else if (condition.check(init).isMet) {
            Some(buildTrans(init))
        } else {
            none()
        }

        ret.map {
            logPageName(init, it)
        }

        return ret
    }

    private fun logPageName(init: InitialTransaction<Request>, opt: PrepareDocumentRequestTransactionImpl<Request>) {
        logger.debug(init.request.getDebugInfo() + " < checking page name: " + opt.parseOption.name.name)
    }

    private fun <Document : Request> isPageNamePreDefined(init: InitialTransaction<Document>): Boolean {
        return init.option.parseOption.isNotEmpty() // Option<ParseOption>.PageName
    }

    private fun <Document : Request> checkPreDefined(init: InitialTransaction<Document>): Option<PrepareDocumentRequestTransactionImpl<Document>> {
        return if (init.option.parseOption.fold({ false }, {
            name == it.name
        })
        ) {
            Some(buildTrans(init))
        } else {
            none()
        }
    }

    private fun <Document : Request> buildTrans(init: InitialTransaction<Document>): PrepareDocumentRequestTransactionImpl<Document> {
        return PrepareDocumentRequestTransactionImpl<Document>(
            init, tagBuilder.build(init), requestOption,
            ParseOption(name), containerOption
        )
    }
}