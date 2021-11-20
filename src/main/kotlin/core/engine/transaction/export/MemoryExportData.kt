package core.engine.transaction.export

import core.engine.ExportData
import core.engine.ExportResultToken
import core.engine.FileIOToken

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