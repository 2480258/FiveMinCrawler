package core.engine

import arrow.core.Option
import core.request.NetworkHeader
import java.lang.Exception
import java.net.URI

suspend inline fun <Return, reified ExpectBodyType> ResponseBody.ifType(
    crossinline corr: suspend (ExpectBodyType) -> Return,
    crossinline el: suspend (ResponseBody) -> Return
): Return {
    if (this is ExpectBodyType) {
        corr(this)
    }

    return el(this)
}

suspend fun <Return> ResponseBody.ifSucc(
    succ: suspend (SuccessBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifType<Return, SuccessBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifRedirect(
    succ: suspend (RedirectResponseBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifType<Return, RedirectResponseBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifAutoRedirect(
    succ: suspend (AutomaticRedirectResponseBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifType<Return, AutomaticRedirectResponseBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifCriticalErr(
    succ: suspend (CriticalErrorBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifType<Return, CriticalErrorBody>(succ, el)
}

suspend fun <Return> ResponseBody.ifRecoverableErr(
    succ: suspend (RecoverableErrorBody) -> Return,
    el: suspend (ResponseBody) -> Return
): Return {
    return this.ifType<Return, RecoverableErrorBody>(succ, el)
}

data class PerformedRequesterInfo(val engine: RequesterEngineInfo, val slot: RequesterSlotInfo) {

}

data class ResponseTime(val sentMS: Long, val receivedMS: Long) {

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