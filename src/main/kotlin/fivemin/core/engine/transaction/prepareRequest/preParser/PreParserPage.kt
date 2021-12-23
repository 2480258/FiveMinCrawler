package fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import fivemin.core.engine.*
import fivemin.core.engine.transaction.PageCondition
import fivemin.core.engine.transaction.PrepareDocumentRequestTransactionImpl
import fivemin.core.engine.transaction.TagBuilder

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
    override fun <Document : Request> makeTransaction(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        if(isPageNamePreDefined(init)){
            return checkPreDefined(init)
        }

        return if(condition.check(init).isMet){
            Some(buildTrans(init))
        } else{
            none()
        }
    }

    private fun <Document : Request> isPageNamePreDefined(init: InitialTransaction<Document>): Boolean {
        return init.option.parseOption.isNotEmpty() //Option<ParseOption>.PageName
    }

    private fun <Document : Request> checkPreDefined(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        return if (init.option.parseOption.fold({ false }, {
                name == it.name
            })) {
            Some(buildTrans(init))
        } else {
            none()
        }
    }

    private fun <Document : Request> buildTrans(init: InitialTransaction<Document>): PrepareTransaction<Document> {
        return PrepareDocumentRequestTransactionImpl<Document>(
            init, tagBuilder.build(init), requestOption,
            ParseOption(name), containerOption
        )
    }

}