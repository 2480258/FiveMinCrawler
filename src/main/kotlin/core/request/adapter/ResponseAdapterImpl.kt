package core.request.adapter

import arrow.core.*
import arrow.core.computations.either
import arrow.core.computations.option
import core.engine.*
import core.request.*
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URI

class ResponseAdapterImpl(
    private val performedRequesterInfo: PerformedRequesterInfo,
    private val factory: MemoryFilterFactory
) : ResponseAdapter {

    private val DEFAULT_BUFFER_SIZE = 32768 //32KB

    override fun createWithError(
        original: core.engine.Request,
        ex : Option<Exception>,
        req: Request
    ) : Validated<Throwable, ResponseBody>{
        return CriticalErrorBodyImpl(createRequestBody(original.target, req), ex).valid()
    }

    override fun createWithReceived(
        original: core.engine.Request,
        resp: Response,
        req: Request
    ): Validated<Throwable, ResponseBody> {
        if (resp.request.url.toUri() != original.target && resp.body != null) {
            return createWithReceived(original, resp, req).map { x ->
                AutomaticRedirectResponseBodyImpl(
                    createRequestBody(original.target, req),
                    resp.code,
                    NetworkHeader(resp.headers.asIterable().toList()),
                    x
                )
            }
        }

        if (resp.body != null && resp.isSuccessful) {
            return createMemoryData(
                resp.body!!,
                original
            ).fold({ HttpNoContentWithSuccessfulException(resp.request.url.toString()).invalid() }) { x ->
                SuccessBodyImpl(
                    createRequestBody(original.target, req),
                    resp.code,
                    NetworkHeader(resp.headers.asIterable().toList()),
                    x,
                    MediaType(resp.body!!.contentType()!!.type, resp.body!!.contentType()!!.subtype),
                    ResponseTime(resp.sentRequestAtMillis, resp.receivedResponseAtMillis)
                ).valid()
            }
        }

        if (resp.body != null && resp.code >= 300 && resp.code <= 399) {
            return resp.headers["Location"].toOption()
                .fold({ HttpNoLocationHeaderWithRedirectCodeException(resp.request.url.toString()).invalid() }) { x ->
                    RedirectResponseBodyImpl(
                        createRequestBody(original.target, req),
                        resp.code,
                        NetworkHeader(resp.headers.asIterable().toList()),
                        URI(x)
                    ).valid()
                }
        }

        return RecoverableErrorBodyImpl(
            createRequestBody(original.target, req),
            resp.code,
            NetworkHeader(resp.headers.asIterable().toList())
        ).valid()
    }

    private fun createRequestBody(originalUri: URI, req: Request): core.engine.RequestBody {
        return core.engine.RequestBody(originalUri, req.url.toUri(), NetworkHeader(req.headers.toList()))
    }

    private fun createMemoryData(responseBody: okhttp3.ResponseBody, request: core.engine.Request): Option<MemoryData> {
        var filter: MemoryFilter? = null

        try {
            var type = responseBody.contentType().toOption()
            var total = if (responseBody.contentLength() != -1L) {
                Some(responseBody.contentLength())
            } else {
                none()
            }

            filter = createMemoryFilter(type, total, request)

            handleStream(responseBody, filter)

            return Some(filter.flushAndExportAndDispose())
        } catch (e: Exception) {
            filter?.close()
        }

        return none()
    }


    private fun handleStream(responseBody: okhttp3.ResponseBody, filter: MemoryFilter) {
        var stream = responseBody.byteStream()
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
        request: core.engine.Request
    ): MemoryFilter {
        return type.zip(total).fold({ factory.createByteFilter(total, request.token) }) {
            if (typeContents(it.first, "html")) {
                factory.createHtmlFilter(it.second.toOption(), request.token)
            } else if (typeContents(it.first, "text") || typeContents(it.first, "json") || typeContents(
                    it.first,
                    "javascript"
                )
            ) {
                factory.createStringFilter(it.second.toOption(), request.token)
            } else {
                factory.createByteFilter(it.second.toOption(), request.token)
            }
        }
    }


    private fun typeContents(type: okhttp3.MediaType, str: String): Boolean {
        return type.type.lowercase().contains(str.lowercase()) || type.subtype.lowercase().contains(str.lowercase())
    }
}

