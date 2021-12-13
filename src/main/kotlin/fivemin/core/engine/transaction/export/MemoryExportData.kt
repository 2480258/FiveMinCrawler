package fivemin.core.engine.transaction.export

import fivemin.core.engine.ExportData
import fivemin.core.engine.ExportResultToken
import fivemin.core.engine.FileIOToken

class MemoryExportData(private val data : ByteArray) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken) : Result<ExportResultToken>{

        return try{
            if(isSaved){
                throw IllegalArgumentException()
            }

            token.openFileWriteStream {
                it.write(data)
                isSaved = true
            }

            Result.success(ExportResultToken(token))
        } catch (e : Exception){
            Result.failure(e)
        }
    }
}