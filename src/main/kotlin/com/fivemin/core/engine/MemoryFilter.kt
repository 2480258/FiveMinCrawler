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

package com.fivemin.core.engine

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import java.io.*
import java.nio.charset.Charset

interface MemoryWriter : MemoryFilter

/**
 * Temp data saver.
 * Saves written data at disk
 */
class DiskWriterImpl constructor(private val token: RequestToken, private val tempPath: DirectoryIOToken) :
    MemoryWriter {
    override var length: Int = 0
    override var isDisposed: Boolean = false
    override var isCompleted: Boolean = false
    
    val TEMP_EXT = ".tmp"
    val writer: FileOutputStream
    val file: FileIOToken
    
    init {
        file = tempPath.withAdditionalPathFile(getAdditionalPath(token))
        writer = file.unsafeOpenFileStream().fold({ x -> throw x }, { x -> x })
    }
    
    private fun getAdditionalPath(tok: RequestToken): String {
        return tok.hashCode().toString() + TEMP_EXT
    }
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        writer.write(b, off, len)
    }
    
    @Log(
        beforeLogLevel = LogLevel.TRACE,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR
    )
    override fun flushAndExportAndDispose(): MemoryData {
        if (isCompleted) {
            throw IllegalStateException()
        }
        
        markAsComplete()
        close()
        
        return FileMemoryDataImpl(file)
    }
    
    private fun markAsComplete() {
        isCompleted = true
    }
    
    override fun close() {
        writer.close()
        isDisposed = true
    }
}

/**
 * Temp data saver.
 * Saves written data to RAM.
 */
class MemoryWriterImpl : MemoryWriter {
    private val writer: ByteArrayOutputStream = ByteArrayOutputStream()
    fun migrateMeToAndDisposeThis(dest: DiskWriterImpl) {
        use {
            writer.writeTo(dest.writer)
        }
    }
    
    override var length: Int = 0
        get() = writer.size()
    override var isDisposed: Boolean = false
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        length += b.size
        writer.write(b, off, len)
    }
    
    override fun flushAndExportAndDispose(): MemoryData {
        writer.flush()
        markAsComplete()
        
        return ArrayMemoryData(writer.toByteArray())
    }
    
    private fun markAsComplete() {
        isCompleted = true
    }
    
    override fun close() {
        writer.close()
        isDisposed = true
    }
    
    override var isCompleted: Boolean = false
}

/**
 * Test purpose memory writer
 */
class ArrayMemoryData constructor(private val data: ByteArray) : MemoryData {
    
    @Log(
        beforeLogLevel = LogLevel.TRACE,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR
    )
    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Either<Throwable, T> {
        val ret = Either.catch { func(ByteArrayInputStream(data)) }
        
        return ret
    }
    
    override fun openWriteStreamUnsafe(): Either<Throwable, OutputStream> {
        TODO("Not yet implemented")
    }
}

/**
 * Decorator of memory writer
 */
interface MemoryFilter : AutoCloseable {
    val length: Int
    
    val isCompleted: Boolean
    val isDisposed: Boolean
    
    fun write(b: ByteArray, off: Int, len: Int)
    fun flushAndExportAndDispose(): MemoryData
}

interface StringFilter : MemoryFilter {
    override fun flushAndExportAndDispose(): StringMemoryData
    val encoding: Charset
}

/**
 * Decorator of memory writer
 * Supports string decodes.
 *
 * Recognizes UTF8, UTF32BE, UTF32LE, UTF16BE, UTF16LE via Byte-Order-Mark
 */
class StringFilterImpl constructor(private val filter: MemoryFilter, private var _encoding: Option<Charset>) :
    StringFilter {
    
    override val encoding: Charset
        get() {
            return _encoding.fold({ Charsets.UTF_8 }, {
                it
            })
        }
    
    val bomCharsets: Map<Charset, ByteArray> = mapOf(
        Charsets.UTF_8 to byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()),
        Charsets.UTF_32BE to byteArrayOf(0x00.toByte(), 0x00.toByte(), 0xFE.toByte(), 0xFF.toByte()),
        Charsets.UTF_32LE to byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00.toByte(), 0x00.toByte()),
        Charsets.UTF_16BE to byteArrayOf(0xFE.toByte(), 0xFF.toByte()),
        Charsets.UTF_16LE to byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    )
    private val MAX_BOM_LENGTH = 10 //how much byte needed to be copied over for BOM recognitions?
    override val isCompleted: Boolean
        get() = filter.isCompleted
    
    override val isDisposed: Boolean
        get() = filter.isDisposed
    
    override val length: Int
        get() = filter.length
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (_encoding.isEmpty() && length == 0) {
            val minCount = Math.min(b.size, MAX_BOM_LENGTH)
            val arr = b.take(minCount).toByteArray()
            _encoding = getEncoding(arr).some()
        }
        
        filter.write(b, off, len)
    }
    
    override fun flushAndExportAndDispose(): StringMemoryData {
        val enc = _encoding.fold({ Charsets.UTF_8 }, { x -> x })
        return StringMemoryDataImpl(filter.flushAndExportAndDispose(), enc)
    }
    
    override fun close() {
        filter.close()
    }
    
    private fun getEncoding(firstByte: ByteArray): Charset {
        return _encoding.getOrElse {
            val candicates = bomCharsets.filterValues {
                firstByte.take(it.size).toByteArray().contentEquals(it)
            }.entries.sortedByDescending {
                it.value.size
            } // Preventing edge case - UTF32LE contains UTF16LE BOM marks....
            
            candicates.firstOrNone().fold({
                Charsets.UTF_8
            }, {
                it.key
            })
        }
    }
}

/**
 * Decorator of memory writer
 * Supports HTML parser.
 */
class HtmlFilterImpl constructor(private val filter: StringFilter, private val factory: HtmlDocumentFactory) :
    MemoryFilter {
    override val isCompleted: Boolean
        get() = filter.isCompleted
    
    override val isDisposed: Boolean
        get() = filter.isDisposed
    
    override val length: Int
        get() = filter.length
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        filter.write(b, off, len)
    }
    
    override fun flushAndExportAndDispose(): HtmlMemoryData {
        return HtmlMemoryDataImpl(filter.flushAndExportAndDispose(), factory)
    }
    
    override fun close() {
        filter.close()
    }
}

/**
 * Memory writer which changes location to be saved.
 * If expectSize > 16KiB or current data > 20KiB, it changes location to disk.
 *
 * @param expectSize Expected size of data
 * @param handle RequestToken to decide temp file name
 * @param tempPath root path for saving those.
 */
class TranslatableFilter(
    private val expectSize: Option<Long>,
    private val handle: RequestToken,
    private val tempPath: DirectoryIOToken
) : MemoryFilter {
    private val MEMORY_BYTE_THRESOLD: Int = 16000
    private val TRANSLATION_THRESOLD: Int = 20000
    
    private var writeStream: MemoryWriter
    
    init {
        writeStream = expectSize.fold({ MemoryWriterImpl() }, {
            if (it < MEMORY_BYTE_THRESOLD) {
                MemoryWriterImpl()
            } else {
                DiskWriterImpl(handle, tempPath)
            }
        })
    }
    
    override val length: Int
        get() = writeStream.length
    override val isCompleted: Boolean
        get() = writeStream.isCompleted
    override val isDisposed: Boolean
        get() = writeStream.isDisposed
    
    override fun write(b: ByteArray, off: Int, len: Int) {
        if (writeStream is MemoryWriterImpl && (writeStream.length > TRANSLATION_THRESOLD)) {
            val ndata = DiskWriterImpl(handle, tempPath)
            (writeStream as MemoryWriterImpl).migrateMeToAndDisposeThis(ndata)
            writeStream = ndata
        }
        
        writeStream.write(b, off, len)
    }
    
    override fun flushAndExportAndDispose(): MemoryData {
        return writeStream.flushAndExportAndDispose()
    }
    
    override fun close() {
        writeStream.close()
    }
    
    fun dispose() {
        writeStream.close()
    }
}
