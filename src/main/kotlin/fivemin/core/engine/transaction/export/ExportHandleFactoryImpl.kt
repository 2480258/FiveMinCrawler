package fivemin.core.engine.transaction.export

import fivemin.core.engine.*
import fivemin.core.export.ExportInfoImpl
import java.io.OutputStream

class ExportHandleFactoryImpl(private val io : DirectIO, private val bookName : String) : ExportHandleFactory {
    private val mainPath : DirectoryIOToken

    init{
        mainPath = io.getToken(UsingPath.EXPORT)
    }

    override fun create(additionalPath: String, ret: OutputStream): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val data : StreamExportData(ret)
    }

    override fun create(additionalPath: String, ret: String): ExportHandle {
        TODO("Not yet implemented")
    }

    override fun create(additionalPath: String, token: FileIOToken): ExportHandle {
        TODO("Not yet implemented")
    }
}