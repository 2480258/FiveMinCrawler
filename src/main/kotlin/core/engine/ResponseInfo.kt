package core.engine

import arrow.core.Option
import core.request.NetworkHeader
import java.lang.Exception
import java.net.URI

inline fun <Return, reified ExpectBodyType> ResponseBody.ifType(
    crossinline corr: (ExpectBodyType) -> Return,
    crossinline el: (ResponseBody) -> Return
): Return {
    if (this is ExpectBodyType) {
        corr(this)
    }

    return el(this)
}

suspend inline fun <Return, reified ExpectBodyType> ResponseBody.ifTypeAsync(
    crossinline corr: suspend (ExpectBodyType) -> Return,
    crossinline el: suspend (ResponseBody) -> Return
): Return {
    if (this is ExpectBodyType) {
        corr(this)
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

data class PerformedRequesterInfo(val engine: RequesterEngineInfo, val slot: RequesterSlotInfo) {

}

data class ResponseTime(val sentMS: Long, val receivedMS: Long) {
    val duration : Long
    get() = sentMS + receivedMS
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
}

interface HttpSuccessBody : SuccessBody, HttpResponseReceivedBody{
    val contentType: MediaType
    val responseTime: ResponseTime
}

interface AutomaticRedirectResponseBody : ResponseBody {
    val afterRedirect: ResponseBody
}

interface HttpAutomaticRedirectResponseBody : AutomaticRedirectResponseBody, HttpResponseReceivedBody {

}

interface RecoverableErrorBody : HttpResponseReceivedBody {

}

interface CriticalErrorBody : ResponseBody {
    val error: Option<Exception>
}

interface RedirectResponseBody : HttpResponseReceivedBody {
    val redirectDest: URI
}

data class RequestBody(
    val originalUri : URI,
    val currentUri: URI,
    val requestHeader: NetworkHeader
) {

}

data class MediaType(val type: String, val subType: String)