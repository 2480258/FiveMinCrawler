package fivemin.core.engine.transaction.export

import arrow.core.Either
import arrow.core.invalid
import fivemin.core.engine.ExportData
import fivemin.core.engine.ExportResultToken
import fivemin.core.engine.FileIOToken
import java.io.InputStream

class StreamExportData(private val data : InputStream) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken) : Either<Throwable, ExportResultToken>{
        return Either.catch {
            data.use { data ->
                if(isSaved || token.exists()){
                    IllegalArgumentException().left()
                }

                token.openFileWriteStream {
                    data.copyTo(it)
                    isSaved = true
                }
            }

            ExportResultToken(token)
        }
    }
}