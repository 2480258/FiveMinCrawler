package fivemin.core.engine.transaction.export

import fivemin.core.engine.*
import fivemin.core.export.ExportInfoImpl
import java.io.InputStream
import java.io.OutputStream

data class ExportHandleImpl(override val request: ExportInfo, override val data: ExportData) : ExportHandle {
    override fun withNewExportInfo(info: ExportInfo): ExportHandle {
        return ExportHandleImpl(info, data)
    }
}

class ExportHandleFactoryImpl(private val io : DirectIO, private val bookName : String) : ExportHandleFactory {
    private val mainPath : DirectoryIOToken

    init{
        mainPath = io.getToken(UsingPath.EXPORT)
    }

    override fun create(additionalPath: String, ret: InputStream): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val data = StreamExportData(ret)

        return ExportHandleImpl(info, data)
    }

    override fun create(additionalPath: String, ret: String): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val array = ret.toByteArray(Charsets.UTF_8)

        val data = MemoryExportData(array)

        return ExportHandleImpl(info, data)
    }

    override fun create(additionalPath: String, token: FileIOToken): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val data = FileInfoExportData(token)

        return ExportHandleImpl(info, data)
    }
}