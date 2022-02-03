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

package com.fivemin.core.engine.transaction

import arrow.core.toOption
import com.fivemin.core.engine.InitialTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.Transaction

interface PageCondition<in Trans : Transaction<Document>, out Document : Request> {
    fun check(trans: Trans): PageConditionResult
}

data class PageConditionResult(val isMet: Boolean)

class UriRegexPageCondition(val regex: Regex) : PageCondition<InitialTransaction<Request>, Request> {
    @OptIn(ExperimentalStdlibApi::class)
    override fun check(trans: InitialTransaction<Request>): PageConditionResult {
        return PageConditionResult(regex.find(trans.request.target.toString()).toOption().fold({ false }, { true }))
    }
}
