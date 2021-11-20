package core.engine.transaction.export

import arrow.core.Validated
import core.engine.ExportData
import core.engine.ExportResultToken
import core.engine.FileIOToken
import java.io.InputStream

class StreamExportData(private val data : InputStream) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken) : Result<ExportResultToken>{
        try{
            data.use { data ->
                if(isSaved || token.exists()){
                    throw IllegalArgumentException()
                }

                token.openFileWriteStream {
                    data.copyTo(it)
                    isSaved = true
                }
            }

            return Result.success(ExportResultToken(token))
        }
        catch (e : Exception){
            return Result.failure(e)
        }
    }
}