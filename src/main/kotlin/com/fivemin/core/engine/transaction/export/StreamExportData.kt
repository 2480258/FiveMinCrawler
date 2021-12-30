package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import arrow.core.left
import com.fivemin.core.engine.ExportData
import com.fivemin.core.engine.ExportResultToken
import com.fivemin.core.engine.FileIOToken
import java.io.InputStream

class StreamExportData(private val data: InputStream) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken): Either<Throwable, ExportResultToken> {
        return Either.catch {
            data.use { data ->
                if (isSaved || token.exists()) {
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
