package com.fivemin.core.request.adapter

import arrow.core.*
import com.fivemin.core.engine.*
import com.fivemin.core.request.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import java.net.URI
import java.nio.charset.Charset

class ResponseAdapterImpl(
    private val performedRequesterInfo: PerformedRequesterInfo,
    private val factory: MemoryFilterFactory
) : ResponseAdapter {

    private val DEFAULT_BUFFER_SIZE = 32768 // 32KB
    private val decompressor = DecompressorImpl()

    override fun createWithError(
        original: com.fivemin.core.engine.Request,
        ex: Option<Throwable>,
        req: Request
    ): Either<Throwable, ResponseBody> {
        return Either.catch {
            CriticalErrorBodyImpl(createRequestBody(original.target, req), ex)
        }
    }

    private fun parseCharset(resp: Response): Option<Charset> {
        return resp.headers["Content-Type"].toOption().map {
            it.toMediaTypeOrNull()?.charset().toOption()
        }.flatten()
    }

    override fun createWithReceived(
        original: com.fivemin.core.engine.Request,
        resp: Response,
        req: Request
    ): Either<Throwable, ResponseBody> {
        val httpTarget = original.target.toHttpUrlOrNull()
        if (httpTarget == null) {
            IllegalArgumentException().left()
        }

        if (resp.request.url != httpTarget && resp.body != null) {
            return createWithReceived(original, resp, req).map { x ->
                AutomaticRedirectResponseBodyImpl(
                    createRequestBody(original.target, req),
                    resp.code,
                    NetworkHeader(resp.headers.asIterable().toList()),
                    x
                )
            }
        }

        if (resp.body != null && resp.code < 299 && resp.code > 199) {
            return createMemoryData(
                resp.body!!,
                original,
                parseCharset(resp),
                resp.headers["Content-Encoding"].toOption()
            ).fold({ HttpNoContentWithSuccessfulException(resp.request.url.toString()).left() }) { x ->
                SuccessBodyImpl(
                    createRequestBody(original.target, req),
                    resp.code,
                    NetworkHeader(resp.headers.asIterable().toList()),
                    x,
                    MediaType(resp.body!!.contentType()!!.type, resp.body!!.contentType()!!.subtype),
                    ResponseTime(resp.sentRequestAtMillis, resp.receivedResponseAtMillis)
                ).right()
            }
        }

        if (resp.body != null && resp.code >= 300 && resp.code <= 399) {
            return resp.headers["Location"].toOption()
                .fold({ HttpNoLocationHeaderWithRedirectCodeException(resp.request.url.toString()).left() }) { x ->
                    RedirectResponseBodyImpl(
                        createRequestBody(original.target, req),
                        resp.code,
                        NetworkHeader(resp.headers.asIterable().toList()),
                        URI(x)
                    ).right()
                }
        }

        return RecoverableErrorBodyImpl(
            createRequestBody(original.target, req),
            resp.code,
            NetworkHeader(resp.headers.asIterable().toList())
        ).right()
    }

    private fun createRequestBody(originalUri: URI, req: Request): com.fivemin.core.engine.RequestBody {
        return com.fivemin.core.engine.RequestBody(originalUri, req.url.toUri(), NetworkHeader(req.headers.toList()))
    }

    private fun createMemoryData(
        responseBody: okhttp3.ResponseBody,
        request: com.fivemin.core.engine.Request,
        enc: Option<Charset>,
        decomp: Option<String>
    ): Option<MemoryData> {
        var filter: MemoryFilter? = null

        try {
            var type = responseBody.contentType().toOption()
            var total = if (responseBody.contentLength() != -1L) {
                Some(responseBody.contentLength())
            } else {
                none()
            }

            filter = createMemoryFilter(type, total, enc, request)

            handleStream(responseBody, filter, decomp)

            return Some(filter.flushAndExportAndDispose())
        } catch (e: Exception) {
            filter?.close()
        }

        return none()
    }

    private fun handleStream(responseBody: okhttp3.ResponseBody, filter: MemoryFilter, decomp: Option<String>) {
        var originalStream = responseBody.byteStream()

        var stream = decomp.fold({ originalStream }) {
            decompressor.decompress(it, originalStream)
        }

        var buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        var isMoreToRead = true

        do {
            var read = stream.readNBytes(buffer, 0, buffer.size)

            if (read == 0) {
                isMoreToRead = false
            } else {
                filter.write(buffer, 0, read)
            }
        } while (isMoreToRead)

        responseBody.close()
    }

    private fun createMemoryFilter(
        type: Option<MediaType>,
        total: Option<Long>,
        enc: Option<Charset>,
        request: com.fivemin.core.engine.Request
    ): MemoryFilter {
        return type.fold(
            {
                factory.createByteFilter(total, request.token)
            }) {
            if (typeContents(it, "html")) {
                factory.createHtmlFilter(total, request.token, enc)
            } else if (typeContents(it, "text") || typeContents(it, "json") || typeContents(
                    it,
                    "javascript"
                )
            ) {
                factory.createStringFilter(total, request.token, enc)
            } else {
                factory.createByteFilter(total, request.token)
            }
        }
    }

    private fun typeContents(type: okhttp3.MediaType, str: String): Boolean {
        return type.type.lowercase().contains(str.lowercase()) || type.subtype.lowercase().contains(str.lowercase())
    }
}
