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

import arrow.core.Either
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset

interface MemoryData {
    fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Either<Throwable, T>

    fun openWriteStreamUnsafe(): Either<Throwable, OutputStream>
}

interface StringMemoryData : MemoryData {
    fun <T> openStreamAsStringAndDispose(func: (InputStreamReader) -> T): Either<Throwable, T>
}

class StringMemoryDataImpl constructor(private val data: MemoryData, private val enc: Charset) : StringMemoryData {
    override fun <T> openStreamAsStringAndDispose(func: (InputStreamReader) -> T): Either<Throwable, T> {
        return data.openStreamAsByteAndDispose { x ->
            func(InputStreamReader(x, enc))
        }
    }

    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Either<Throwable, T> {
        return data.openStreamAsByteAndDispose(func)
    }

    override fun openWriteStreamUnsafe(): Either<Throwable, OutputStream> {
        return data.openWriteStreamUnsafe()
    }
}

interface HtmlMemoryData : StringMemoryData {
    fun <T> parseAsHtmlDocument(func: (HtmlParsable) -> T): Either<Throwable, T>
    suspend fun <T> parseAsHtmlDocumentAsync(func: suspend (HtmlParsable) -> T): Either<Throwable, T>
}

class HtmlMemoryDataImpl constructor(private val data: StringMemoryData, private val fac: HtmlDocumentFactory) : HtmlMemoryData {
    val doc: Lazy<Either<Throwable, HtmlParsable>> = lazy {
        data.openStreamAsStringAndDispose {
            x ->
            fac.create(x)
        }
    }
    
    override fun <T> parseAsHtmlDocument(func: (HtmlParsable) -> T): Either<Throwable, T> {
        return doc.value.map { x -> func(x) }
    }

    override suspend fun <T> parseAsHtmlDocumentAsync(func: suspend (HtmlParsable) -> T): Either<Throwable, T> {
        return doc.value.map { x -> func(x) }
    }

    override fun <T> openStreamAsStringAndDispose(func: (InputStreamReader) -> T): Either<Throwable, T> {
        return data.openStreamAsStringAndDispose(func)
    }

    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Either<Throwable, T> {
        return data.openStreamAsByteAndDispose(func)
    }

    override fun openWriteStreamUnsafe(): Either<Throwable, OutputStream> {
        return data.openWriteStreamUnsafe()
    }
}

interface FileMemoryData : MemoryData {
    val file: FileIOToken
}

class FileMemoryDataImpl constructor(override val file: FileIOToken) : FileMemoryData {
    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Either<Throwable, T> {
        return file.openFileReadStream(func)
    }

    override fun openWriteStreamUnsafe(): Either<Throwable, FileOutputStream> {
        return file.unsafeOpenFileStream()
    }
}

suspend fun <T> MemoryData.ifHtmlAsync(action: suspend (HtmlMemoryData) -> T, el: suspend (MemoryData) -> T): T {
    if (this is HtmlMemoryData) {
        return action(this)
    }

    return el(this)
}

fun <T> MemoryData.ifHtml(action: (HtmlMemoryData) -> T, el: (MemoryData) -> T): T {
    if (this is HtmlMemoryData) {
        return action(this)
    }

    return el(this)
}

fun <T> MemoryData.ifString(action: (StringMemoryData) -> T, el: (MemoryData) -> T): T {
    if (this is StringMemoryData) {
        return action(this)
    }

    return el(this)
}

fun <T> MemoryData.ifFile(action: (FileMemoryData) -> T, el: (MemoryData) -> T): T {
    if (this is FileMemoryData) {
        return action(this)
    }

    return el(this)
}
