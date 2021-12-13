package fivemin.core.engine

import arrow.core.Validated
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset

interface MemoryData {
    fun <T> openStreamAsByteAndDispose(func : (InputStream) -> T) : Validated<Throwable, T>

    fun openWriteStreamUnsafe() : Validated<Throwable, OutputStream>
}

interface StringMemoryData : MemoryData{
    fun <T> openStreamAsStringAndDispose(func : (InputStreamReader) -> T) : Validated<Throwable, T>
}

class StringMemoryDataImpl constructor(private val data : MemoryData, private val enc : Charset) : StringMemoryData{
    override fun <T> openStreamAsStringAndDispose(func: (InputStreamReader) -> T): Validated<Throwable, T> {
        return data.openStreamAsByteAndDispose { x ->
            func(InputStreamReader(x, enc))
        }
    }

    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Validated<Throwable, T> {
        return data.openStreamAsByteAndDispose(func)
    }

    override fun openWriteStreamUnsafe(): Validated<Throwable, OutputStream> {
        return data.openWriteStreamUnsafe()
    }

}

interface HtmlMemoryData : StringMemoryData{
    fun <T> parseAsHtmlDocument(func : (HtmlParsable) -> T) : Validated<Throwable, T>
}

class HtmlMemoryDataImpl constructor(private val data : StringMemoryData, private val fac : HtmlDocumentFactory) : HtmlMemoryData{
    val doc : Lazy<Validated<Throwable, HtmlParsable>>

    init {
        doc = lazy {
             data.openStreamAsStringAndDispose {
                     x -> fac.create(x) }
        }

    }

    override fun <T> parseAsHtmlDocument(func: (HtmlParsable) -> T): Validated<Throwable, T> {
        return doc.value.map {x -> func(x) }
    }

    override fun <T> openStreamAsStringAndDispose(func: (InputStreamReader) -> T): Validated<Throwable, T> {
        return data.openStreamAsStringAndDispose(func)
    }

    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Validated<Throwable, T> {
        return data.openStreamAsByteAndDispose(func)
    }

    override fun openWriteStreamUnsafe(): Validated<Throwable, OutputStream> {
        return data.openWriteStreamUnsafe()
    }

}

interface FileMemoryData : MemoryData{
    val file : FileIOToken
}

class FileMemoryDataImpl constructor(override val file : FileIOToken) : FileMemoryData{
    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Validated<Throwable, T> {
        return file.openFileReadStream(func)
    }

    override fun openWriteStreamUnsafe(): Validated<Throwable, FileOutputStream> {
        return file.unsafeOpenFileStream()
    }
}

fun <T> MemoryData.ifHtml(action : (HtmlMemoryData) -> T, el : (MemoryData) -> T) : T{
    if(this is HtmlMemoryData){
        return action(this)
    }

    return el(this)
}

fun <T> MemoryData.ifString(action : (StringMemoryData) -> T, el : (MemoryData) -> T) : T{
    if(this is StringMemoryData){
        return action(this)
    }

    return el(this)
}

fun <T> MemoryData.ifFile(action : (FileMemoryData) -> T, el : (MemoryData) -> T) : T{
    if(this is FileMemoryData){
        return action(this)
    }

    return el(this)
}