package core.request.adapter

import arrow.core.Option
import arrow.core.Some
import arrow.core.computations.either
import arrow.core.computations.option
import arrow.core.none
import arrow.core.toOption
import core.engine.*
import core.request.AutomaticRedirectResponseBodyImpl
import core.request.MemoryFilterFactory
import core.request.NetworkHeader
import core.request.SuccessBodyImpl
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URI

class ResponseAdapterImpl(
    private val performedRequesterInfo: PerformedRequesterInfo,
    private val factory: MemoryFilterFactory
) {

    private val DEFAULT_BUFFER_SIZE = 32768 //32KB

    fun create(original: HttpRequest, resp: Response, req: Option<Request>): ResponseBody {

    }

    private fun createInfo(original: HttpRequest, resp: Response, req: Option<Request>): ResponseBody {
        req.map {
            if (resp.request.url.toUri() != original.target && resp.body != null) {
                return AutomaticRedirectResponseBodyImpl(
                    createRequestBody(original.target, it),
                    resp.code,
                    NetworkHeader(resp.headers.asIterable().toList()),
                    createInfo(original, resp, req)
                )
            }

            if (resp.body != null && resp.isSuccessful) {
                createMemoryData(resp.body!!, original).map { x ->
                    SuccessBodyImpl(
                        createRequestBody(original.target, it),
                        resp.code,
                        NetworkHeader(resp.headers.asIterable().toList()),
                        x,
                        MediaType(resp.body!!.contentType()!!.type, resp.body!!.contentType()!!.subtype),
                        ResponseTime(resp.sentRequestAtMillis, resp.receivedResponseAtMillis)
                    )
                }

                return
            }


        }

    }

    private fun createRequestBody(originalUri: URI, req: Request): core.engine.RequestBody {
        return core.engine.RequestBody(originalUri, req.url.toUri(), NetworkHeader(req.headers.toList()))
    }

    private fun createMemoryData(responseBody: okhttp3.ResponseBody, request: HttpRequest): Option<MemoryData> {
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
        request: HttpRequest
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

