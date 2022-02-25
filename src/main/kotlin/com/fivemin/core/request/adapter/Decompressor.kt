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

import arrow.core.toOption
import org.brotli.dec.BrotliInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * Decompresses streams with given content type.
 */
interface Decompressor {
    fun decompress(contentType: String, stream: InputStream): InputStream
}

/**
 * Decompresses streams with given content type.
 *
 * Supported: br for Brotli, gzip for GZIP, deflate for Deflate
 */
class DecompressorImpl : Decompressor {
    val decoderMap =
        mapOf("br" to DecompressMethod.BR, "gzip" to DecompressMethod.GZIP, "deflate" to DecompressMethod.DEFLATE)

    override fun decompress(contentType: String, stream: InputStream): InputStream {
        var ret = decoderMap[contentType.lowercase()].toOption()

        return ret.fold({ stream }) {
            when (it) {
                DecompressMethod.DEFLATE -> {
                    InflaterInputStream(stream)
                }

                DecompressMethod.GZIP -> {
                    GZIPInputStream(stream)
                }

                DecompressMethod.BR -> {
                    BrotliInputStream(stream)
                }

                else -> {
                    stream
                }
            }
        }
    }
}

enum class DecompressMethod {
    BR, GZIP, DEFLATE
}
