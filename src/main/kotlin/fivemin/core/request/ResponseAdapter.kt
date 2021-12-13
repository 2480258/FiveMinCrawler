package fivemin.core.request

import arrow.core.Option
import arrow.core.Validated
import fivemin.core.engine.HttpRequest
import fivemin.core.engine.ResponseBody
import okhttp3.Request
import okhttp3.Response

interface ResponseAdapter {
    fun createWithError(
        original: fivemin.core.engine.Request,
        ex: Option<Exception>,
        req: Request
    ): Validated<Throwable, ResponseBody>

    fun createWithReceived(
        original: fivemin.core.engine.Request,
        resp: Response,
        req: Request
    ): Validated<Throwable, ResponseBody>
}