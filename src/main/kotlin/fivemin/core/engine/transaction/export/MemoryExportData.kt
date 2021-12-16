package fivemin.core.engine.transaction.export

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import fivemin.core.engine.ExportData
import fivemin.core.engine.ExportResultToken
import fivemin.core.engine.FileIOToken

class MemoryExportData(private val data: ByteArray) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken): Validated<Throwable, ExportResultToken> {

        return Validated.catch {
            if (isSaved) {
                IllegalArgumentException().invalid()
            }

            token.openFileWriteStream {
                it.write(data)
                isSaved = true
            }

            ExportResultToken(token)
        }
    }
}