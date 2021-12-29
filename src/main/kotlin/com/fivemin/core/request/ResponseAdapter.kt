package com.fivemin.core.request

import arrow.core.Option
import arrow.core.Either
import com.fivemin.core.engine.HttpRequest
import com.fivemin.core.engine.ResponseBody
import okhttp3.Request
import okhttp3.Response

interface ResponseAdapter {
    fun createWithError(
        original: com.fivemin.core.engine.Request,
        ex: Option<Exception>,
        req: Request
    ): Either<Throwable, ResponseBody>

    fun createWithReceived(
        original: com.fivemin.core.engine.Request,
        resp: Response,
        req: Request
    ): Either<Throwable, ResponseBody>
}