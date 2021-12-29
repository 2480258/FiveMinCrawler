package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.ResponseBody
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select

class DefaultRequesterCore(
    override val extraInfo: RequesterExtra,
    private val info: PerformedRequesterInfo,
    config: HttpRequesterConfig,
    adapter: RequesterAdapter
) : RequesterCore<HttpResponseMessage> {
    private val procedure: HttpRequestProcedure

    init {
        procedure = HttpRequestProcedure(info, config, adapter)
    }

    override suspend fun request(request: DequeuedRequest): Deferred<Either<Throwable, HttpResponseMessage>> {
        return coroutineScope {
            async{
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