/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
        private val logger = LoggerController.getLogger("PreParserPageImpl")
    }

    override fun <Document : Request> makeTransaction(init: InitialTransaction<Document>): Option<PrepareTransaction<Document>> {
        val isPreDefined = if (isPageNamePreDefined(init)) {
            checkPreDefined(init)
        } else if (condition.check(init).isMet) {
            Some(buildTrans(init))
        } else {
            none()
        }

        isPreDefined.map {
            logPageName(init, it)
        }

        return isPreDefined
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
