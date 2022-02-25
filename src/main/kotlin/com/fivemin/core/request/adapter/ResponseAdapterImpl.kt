/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.request.adapter

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.request.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import java.net.URI
import java.nio.charset.Charset

/**
 * Response Adapter for okhttp
 */
class ResponseAdapterImpl(
    private val performedRequesterInfo: PerformedRequesterInfo,
    private val factory: MemoryFilterFactory
) : ResponseAdapter {

    companion object {
        private val logger = LoggerController.getLogger("ResponseAdapterImpl")
    }

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
        return resp.headers["Content-Type"].toOption().map { //Content-Type means decompress algorithm.
            it.toMediaTypeOrNull()?.charset().toOption()
        }.flatten()
    }

    override fun createWithReceived(
        original: com.fivemin.core.engine.Request,
        resp: Response,
        req: Request
    ): Either<Throwable, ResponseBody> {
        resp.use { response ->
            val httpTarget = original.target.toHttpUrlOrNull()
            if (httpTarget == null) {
                IllegalArgumentException().left()
            }
            
            //if original URL != response URL, then it should be redirected.
            if (response.request.url != httpTarget && response.body != null) {
                return createWithReceived(original, response, req).map { x ->
                    AutomaticRedirectResponseBodyImpl(
                        createRequestBody(original.target, req),
                        response.code,
                        NetworkHeader(response.headers.asIterable().toList()),
                        x
                    )
                }
            }

            //when successes.
            if (response.body != null && response.code < 299 && response.code > 199) {
                return createMemoryData(
                    response.body!!,
                    original,
                    parseCharset(response),
                    response.headers["Content-Encoding"].toOption()
                ).fold({ HttpNoContentWithSuccessfulException(response.request.url.toString()).left() }) { x ->
                    SuccessBodyImpl(
                        createRequestBody(original.target, req),
                        response.code,
                        NetworkHeader(response.headers.asIterable().toList()),
                        x,
                        MediaType(response.body!!.contentType()!!.type, response.body!!.contentType()!!.subtype),
                        ResponseTime(response.sentRequestAtMillis, response.receivedResponseAtMillis)
                    ).right()
                }
            }

            //redirects
            if (response.body != null && response.code >= 300 && response.code <= 399) {
                return response.headers["Location"].toOption()
                    .fold({ HttpNoLocationHeaderWithRedirectCodeException(response.request.url.toString()).left() }) { x ->
                        RedirectResponseBodyImpl(
                            createRequestBody(original.target, req),
                            response.code,
                            NetworkHeader(response.headers.asIterable().toList()),
                            URI(x)
                        ).right()
                    }
            }

            //other 1XX or 4XX or 5XX is considered error.
            return RecoverableErrorBodyImpl(
                createRequestBody(original.target, req),
                response.code,
                NetworkHeader(response.headers.asIterable().toList())
            ).right()
        }
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
            val result = Either.catch {
                val type = responseBody.contentType().toOption()
                val total = if (responseBody.contentLength() != -1L) {
                    Some(responseBody.contentLength())
                } else {
                    none()
                }

                filter = createMemoryFilter(type, total, enc, request)

                handleStream(responseBody, filter!!, decomp)

                filter!!.flushAndExportAndDispose()
            }

            result.swap().map {
                logger.warn(it)
            }

            return result.orNone()
        } finally {
            filter?.close()
        }
    }

    private fun handleStream(responseBody: okhttp3.ResponseBody, filter: MemoryFilter, decomp: Option<String>) {
        val originalStream = responseBody.byteStream()

        val stream = decomp.fold({ originalStream }) {
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
