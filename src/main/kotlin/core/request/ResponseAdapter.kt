package core.request

import arrow.core.Option
import arrow.core.Validated
import core.engine.HttpRequest
import core.engine.ResponseBody
import okhttp3.Request
import okhttp3.Response

interface ResponseAdapter {
    fun createWithError(
        original: core.engine.Request,
        ex: Option<Exception>,
        req: Request
    ): Validated<Throwable, ResponseBody>

    fun createWithReceived(
        original: core.engine.Request,
        resp: Response,
        req: Request
    ): Validated<Throwable, ResponseBody>
}