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
        val globalConditionMet = globalCondition.check(init).isMet

        return if (globalConditionMet && init.request.requestType == RequestType.LINK) {
            val parsedResult = pages.map {
                Pair(it, it.makeTransaction(init))
            }

            logPages(parsedResult, init)

            parsedResult.map {
                it.second
            }.filterOption().singleOrNone()
        } else if (globalConditionMet) {
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
