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

package com.fivemin.core.request

import arrow.core.Either
import arrow.core.right
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import java.util.*

class RequesterSelectorImpl(val requesterMap: Map<RequesterEngineInfo, RequesterEngine<ResponseData>>) :
    RequesterSelector {
    var rd = Random(System.currentTimeMillis())

    override fun <Document : Request, Resp : ResponseData> schedule(req: DocumentRequest<Document>): Either<Throwable, RequesterSelected<Resp>> {
        var pref = req.request.requestOption.preference
        var engine = getEngine<Resp>(pref.engine)

        return engine.map {
            var idx = pref.slot.fold({ randomSelect(it) }, {
                it
            })

            var ret = getCore(it, idx)

            RequesterSelected(ret, PerformedRequesterInfo(pref.engine, idx))
        }
    }

    private fun <Resp : ResponseData> getEngine(info: RequesterEngineInfo): Either<Throwable, RequesterEngine<Resp>> {
        return (requesterMap[info]!! as RequesterEngine<Resp>).right() // we wouldn't check type; if wrong restart is required anyway
    }

    private fun <Resp : ResponseData> getCore(
        engine: RequesterEngine<Resp>,
        info: RequesterSlotInfo
    ): RequesterCore<Resp> {
        return engine.get(info)
    }

    private fun <Resp : ResponseData> randomSelect(req: RequesterEngine<Resp>): RequesterSlotInfo {
        var idx = rd.nextInt() % req.count
        return RequesterSlotInfo(idx)
    }
}
