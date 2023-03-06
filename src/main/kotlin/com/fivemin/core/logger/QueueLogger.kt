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

package com.fivemin.core.logger

import arrow.core.Option
import com.fivemin.core.engine.Request
import com.fivemin.core.request.PreprocessedRequest
import com.fivemin.core.request.queue.EnqueuedRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class NetworkReport {

}

interface WebSocketLogger {
    fun logViaNetworkEnqueue(request: Request, score: Double)
    
    fun logViaNetworkDequeue(request: Request)
}

@Suppress("unused")
@Aspect
class QueueLogger {
    companion object {
        var webSocketLogger: WebSocketLogger? = null
    }
    @Suppress("unused")
    @Before("@annotation(NetworkReport) && call(* enqueueWithScore*(*, *, *, *))")
    fun enqueueLogToNetwork(joinPoint: JoinPoint) {
        // cast variables before checking null; to help finding type errors early
        val req = joinPoint.args[1] as PreprocessedRequest<Request>
        val score = joinPoint.args[3] as Double
        
        webSocketLogger?.logViaNetworkEnqueue(req.request.request.request, score)
    }
    @Suppress("unused")
    @AfterReturning("@annotation(NetworkReport) && call(* removeFirstFromQueue*())", returning = "retVal")
    fun dequeueLogToWebSocket(retVal: Any) {
        val ret = retVal as Option<EnqueuedRequest<Request>>
        ret.map {
            webSocketLogger?.logViaNetworkDequeue(it.request.request.request.request)
        }
    }
}