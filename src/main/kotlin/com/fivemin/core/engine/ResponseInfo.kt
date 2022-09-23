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

package com.fivemin.core.engine

import arrow.core.Option
import com.fivemin.core.request.NetworkHeader
import java.lang.Exception
import java.net.URI
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

inline fun <Return, reified ExpectBodyType> ResponseBody.ifType(
    crossinline corr: (ExpectBodyType) -> Return,
    crossinline el: (ResponseBody) -> Return
): Return {
    if (this is ExpectBodyType) {
        var ret = corr(this)

        return ret
    }

    return el(this)
}

suspend inline fun <Return, reified ExpectBodyType> ResponseBody.ifTypeAsync(
    crossinline corr: suspend (ExpectBodyType) -> Return,
    crossinline el: suspend (ResponseBody) -> Return
): Return {
    if (this is ExpectBodyType) {
        return corr(this)
    }

    return el(this)
}

fun <Return> ResponseBody.ifHttpSucc(
    succ: (HttpSuccessBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType(succ, el)
}

fun <Return> ResponseBody.ifSucc(
    succ: (SuccessBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType<Return, SuccessBody>(succ, el)
}

fun <Return> ResponseBody.ifRedirect(
    succ: (RedirectResponseBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType<Return, RedirectResponseBody>(succ, el)
}

fun <Return> ResponseBody.ifAutoRedirect(
    succ: (AutomaticRedirectResponseBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType<Return, AutomaticRedirectResponseBody>(succ, el)
}

fun <Return> ResponseBody.ifCriticalErr(
    succ: (CriticalErrorBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType<Return, CriticalErrorBody>(succ, el)
}

fun <Return> ResponseBody.ifRecoverableErr(
    succ: (RecoverableErrorBody) -> Return,
    el: (ResponseBody) -> Return
): Return {
    return this.ifType<Return, RecoverableErrorBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifHttpSuccAsync(
    succ: suspend (HttpSuccessBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync(succ, el)
}

suspend fun <Return> ResponseBody.ifSuccAsync(
    succ: suspend (SuccessBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync<Return, SuccessBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifRedirectAsync(
    succ: suspend (RedirectResponseBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync<Return, RedirectResponseBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifAutoRedirectAsync(
    succ: suspend (AutomaticRedirectResponseBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync<Return, AutomaticRedirectResponseBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifCriticalErrAsync(
    succ: suspend (CriticalErrorBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync<Return, CriticalErrorBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifRecoverableErrAsync(
    succ: suspend (RecoverableErrorBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifTypeAsync<Return, RecoverableErrorBody>(succ, el)
}

data class PerformedRequesterInfo(val engine: RequesterEngineInfo, val slot: RequesterSlotInfo)

data class ResponseTime(val sentMS: Long, val receivedMS: Long) {
    @OptIn(ExperimentalTime::class)
    val duration: Duration
        get() = Duration.milliseconds(Math.max(0, receivedMS - sentMS))
}

interface ResponseData {
    val responseBody: ResponseBody
    val requesterInfo: PerformedRequesterInfo
    fun releaseRequester()
}

interface ResponseBody {
    val requestBody: RequestBody
}

interface HttpResponseReceivedBody : ResponseBody {
    val code: Int
    val responseHeader: NetworkHeader
}

interface SuccessBody : ResponseBody {
    val body: MemoryData
    val responseTime: ResponseTime
}

interface HttpSuccessBody : SuccessBody, HttpResponseReceivedBody {
    val contentType: MediaType
}

interface AutomaticRedirectResponseBody : ResponseBody {
    val afterRedirect: ResponseBody
}

interface HttpAutomaticRedirectResponseBody : AutomaticRedirectResponseBody, HttpResponseReceivedBody

interface RecoverableErrorBody : HttpResponseReceivedBody

interface CriticalErrorBody : ResponseBody {
    val error: Option<Throwable>
}

interface RedirectResponseBody : HttpResponseReceivedBody {
    val redirectDest: URI
}

interface CanceledResponseBody : ResponseBody {

}

data class RequestBody(
    val originalUri: URI,
    val currentUri: URI,
    val requestHeader: NetworkHeader
)

data class MediaType(val type: String, val subType: String)
