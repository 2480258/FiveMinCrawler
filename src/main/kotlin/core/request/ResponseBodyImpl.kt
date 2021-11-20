package core.request

import arrow.core.Option
import core.engine.*
import java.lang.Exception
import java.net.URI

class AutomaticRedirectResponseBodyImpl(
    override val requestBody: RequestBody,
    override val code: Int,
    override val responseHeader: NetworkHeader,
    override val afterRedirect: ResponseBody
) : AutomaticRedirectResponseBody {
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
    override val responseHeader: NetworkHeader,
    override val reason: String
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
) : SuccessBody{

}