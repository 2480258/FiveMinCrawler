package fivemin.core.request

import arrow.core.Option
import fivemin.core.engine.*
import java.lang.Exception
import java.net.URI

class AutomaticRedirectResponseBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val afterRedirect: ResponseBody
) : HttpAutomaticRedirectResponseBody {
}

class RedirectResponseBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val redirectDest: URI
) : RedirectResponseBody {

}

class RecoverableErrorBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader
) : RecoverableErrorBody{

}

class CriticalErrorBodyImpl(override val requestBody: RequestBody, override val error: Option<Exception>) : CriticalErrorBody{

}

class SuccessBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val body: MemoryData,
    override val contentType: MediaType,
    override val responseTime: ResponseTime
) : HttpSuccessBody{

}