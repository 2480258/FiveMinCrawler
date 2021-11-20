package core.engine

import arrow.core.Option
import core.request.NetworkHeader
import okhttp3.RequestBody
import okhttp3.Response
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

data class UnixTimeStamp(val timestamp: Long) {

}

data class ResponseTime(val sent: UnixTimeStamp, val recived: UnixTimeStamp) {

}

interface ResponseData {
    val responseBody: ResponseBody
    val requesterInfo: PerformedRequesterInfo
    fun releaseRequester()
}

interface ResponseBody {
    val requestBody : RequestBody
}

interface ResponseReceivedBody : ResponseBody{
    val code : Int
    val responseHeader : NetworkHeader
}

interface SuccessBody : ResponseReceivedBody {
    val body: MemoryData
    val contentType : MediaType
    val responseTime : ResponseTime
}

interface AutomaticRedirectResponseBody : ResponseReceivedBody {
    val afterRedirect: ResponseBody
}

interface RecoverableErrorBody : ResponseReceivedBody {
    val reason: String
}

interface CriticalErrorBody : ResponseBody {
    val error: Option<Exception>
}

interface RedirectResponseBody : ResponseReceivedBody {
    val redirectDest: URI
}

interface RequestBody {
    val currentUri: URI
    val requestHeader: NetworkHeader
}

data class MediaType (val type: String, val subType: String)