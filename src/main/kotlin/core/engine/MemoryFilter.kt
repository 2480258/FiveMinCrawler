package core.engine

import arrow.core.*
import java.io.*
import java.nio.charset.Charset


//WIP

interface MemoryWriter : MemoryFilter {

}

class DiskWriterImpl constructor(private val token : RequestToken, private val tempPath: DirectoryIOToken): MemoryWriter{
    override var length: Int = 0
    override var isDisposed: Boolean = false
    override var isCompleted: Boolean = false

    val TEMP_EXT = ".tmp"
    val writer :FileOutputStream
    val file : FileIOToken
    init {
        file = tempPath.withAdditionalPathFile(getAdditionalPath(token))
        writer = file.unsafeOpenFileStream().fold({x -> throw x}, {x -> x})
    }

    private fun getAdditionalPath(tok : RequestToken) : String{
        return tok.hashCode().toString() + TEMP_EXT
    }

    override fun write(b: ByteArray) {
        writer.write(b)
    }

    override fun flushAndExportAndDispose() : MemoryData{
        if(isCompleted){
            throw IllegalStateException()
        }

        markAsComplete()
        close()

        return FileMemoryDataImpl(file)
    }

    private fun markAsComplete(){
        isCompleted = true
    }

    override fun close() {
        writer.close()
        isDisposed = true
    }

}

class MemoryWriterImpl : MemoryWriter{
    private val writer : ByteArrayOutputStream
    fun migrateMeToAndDisposeThis(dest: DiskWriterImpl) {
        try{
            writer.writeTo(dest.writer)
        }finally {
            close()
        }
    }

    override var length : Int = 0
        get() = writer.size()
    override var isDisposed: Boolean = false

    override fun write(b: ByteArray) {
        length += b.size
        writer.write(b)
    }

    override fun flushAndExportAndDispose() : MemoryData {
        writer.flush()
        markAsComplete()

        return ArrayMemoryData(writer.toByteArray())
    }

    private fun markAsComplete(){
        isCompleted = true
    }

    override fun close() {
        writer.close()
        isDisposed = true
    }

    override var isCompleted: Boolean = false

    init{
        writer = ByteArrayOutputStream()
    }
}

class ArrayMemoryData constructor(private val data : ByteArray) : MemoryData{
    override fun <T> openStreamAsByteAndDispose(func: (InputStream) -> T): Validated<Throwable, T> {
        return Validated.catch{func(ByteArrayInputStream(data))}
    }

    override fun openWriteStreamUnsafe(): Validated<Throwable, OutputStream> {
        TODO("Not yet implemented")
    }
}

interface MemoryFilter : AutoCloseable {
    var length: Int

    var isCompleted : Boolean
    var isDisposed : Boolean

    fun write(b : ByteArray)

    fun flushAndExportAndDispose() : MemoryData
}

interface StringFilter : MemoryFilter{
    override fun flushAndExportAndDispose(): StringMemoryData
}

class StringFilterImpl constructor(private val filter : MemoryFilter, private var encoding : Option<Charset>) : StringFilter{

    val bomCharsets : Map<Charset, ByteArray> = mapOf(
        Charsets.UTF_8 to byteArrayOf(0xEF.toByte(),0xBB.toByte(),0xBF.toByte()),
        Charsets.UTF_32BE to byteArrayOf(0x00.toByte(),0x00.toByte(),0xFE.toByte(),0xFF.toByte()),
        Charsets.UTF_32LE to byteArrayOf(0xFF.toByte(),0xFE.toByte(),0x00.toByte(),0x00.toByte()),
        Charsets.UTF_16BE to byteArrayOf(0xFE.toByte(),0xFF.toByte()),
        Charsets.UTF_16LE to byteArrayOf(0xFF.toByte(),0xFE.toByte())
            )
    private val MAX_BOM_LENGTH = 10
    override var isCompleted: Boolean = false
        get() = filter.isCompleted

    override var isDisposed: Boolean = false
        get() = filter.isDisposed

    override var length: Int = 0
        get() = filter.length

    override fun write(b: ByteArray) {
        if(encoding.isNotEmpty() && length == 0){
            val minCount = Math.min(b.size, MAX_BOM_LENGTH)
            val arr = b.take(minCount).toByteArray()
            encoding = getEncoding(arr).some()
        }

        filter.write(b)
    }

    override fun flushAndExportAndDispose(): StringMemoryData {
        val enc = encoding.fold({ Charsets.UTF_8}, {x -> x})
        return StringMemoryDataImpl(filter.flushAndExportAndDispose(), enc)
    }

    override fun close() {
        filter.close()
    }

    private fun getEncoding(firstByte : ByteArray) : Charset{
        return encoding.getOrElse {
            bomCharsets.filterValues {
                firstByte.take(it.size).toByteArray().contentEquals(it)
            }.entries.singleOrNone().fold({
                Charsets.UTF_8
            },{
                it.key
            })
        }

    }
}

class HtmlFilterImpl constructor(private val filter : StringFilter, private val factory: HtmlDocumentFactory) : MemoryFilter{
    override var isCompleted: Boolean = false
        get() = filter.isCompleted

    override var isDisposed: Boolean = false
        get() = filter.isDisposed

    override var length: Int = 0
        get() = filter.length

    override fun write(b: ByteArray) {
        filter.write(b)
    }

    override fun flushAndExportAndDispose(): HtmlMemoryData {
        return HtmlMemoryDataImpl(filter.flushAndExportAndDispose(), factory)
    }

    override fun close() {
        filter.close()
    }

}

class TranslatableFilter(private val expectSize : Option<Int>,
                         private val handle : RequestToken,
                         private val tempPath : DirectoryIOToken
)
{
    private val MEMORY_BYTE_THRESOLD : Int = 8192
    private val TRANSLATION_THRESOLD : Int = 10000

    private var writeStream : MemoryWriter

    init{
        writeStream = expectSize.fold({MemoryWriterImpl()}, {
            if(it < MEMORY_BYTE_THRESOLD){
                MemoryWriterImpl()
            }
            else{
                DiskWriterImpl(handle, tempPath)
            }
        })
    }

    fun write(buffer : ByteArray){
        if(writeStream is MemoryWriterImpl && (writeStream.length > TRANSLATION_THRESOLD)){
            val ndata = DiskWriterImpl(handle, tempPath)
            (writeStream as MemoryWriterImpl).migrateMeToAndDisposeThis(ndata)
            writeStream = ndata
        }

        writeStream.write(buffer)
    }

    fun flushAndDispose() : MemoryData{
        return writeStream.flushAndExportAndDispose()
    }

    fun dispose(){
        writeStream.close()
    }
}