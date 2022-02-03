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
import com.fivemin.core.engine.PerformedRequesterInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select

class DefaultRequesterCore(
    override val extraInfo: RequesterExtra,
    private val info: PerformedRequesterInfo,
    config: RequesterConfig,
    adapter: RequesterAdapter
) : RequesterCore<HttpResponseMessage> {
    private val procedure: HttpRequestProcedure

    init {
        procedure = HttpRequestProcedure(info, config, adapter)
    }

    override suspend fun request(request: DequeuedRequest): Deferred<Either<Throwable, HttpResponseMessage>> {
        return coroutineScope {
            async {
                val ret = procedure.request(request.request.request.request.request)

                select {
                    ret.onAwait.invoke {
                        it.map {
                            HttpResponseMessage(it, info)
                        }
                    }
                }
            }
        }
    }
}
