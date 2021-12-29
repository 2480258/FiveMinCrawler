package com.fivemin.core.request.adapter

import arrow.core.toOption
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream
import org.brotli.dec.BrotliInputStream

interface Decompressor {
    fun decompress(contentType: String, stream: InputStream): InputStream
}

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