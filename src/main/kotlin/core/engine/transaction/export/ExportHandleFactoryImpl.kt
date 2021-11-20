package core.engine.transaction.export

import core.engine.*
import java.io.OutputStream

class ExportHandleFactoryImpl(private val io : DirectIO, private val bookName : String) : ExportHandleFactory {
    private val mainPath : DirectoryIOToken

    init{
        mainPath = io.getToken(UsingPath.EXPORT)
    }

    override fun create(additionalPath: String, ret: OutputStream): ExportHandle {
        TODO("Not yet implemented")
    }

    override fun create(additionalPath: String, ret: String): ExportHandle {
        TODO("Not yet implemented")
    }

    override fun create(additionalPath: String, token: FileIOToken): ExportHandle {
        TODO("Not yet implemented")
    }
}