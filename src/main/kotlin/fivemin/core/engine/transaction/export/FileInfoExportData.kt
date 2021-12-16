package fivemin.core.engine.transaction.export

import arrow.core.Validated
import arrow.core.valid
import fivemin.core.engine.ExportData
import fivemin.core.engine.ExportResultToken
import fivemin.core.engine.FileIOToken

class FileInfoExportData (val token : FileIOToken) : ExportData{
    override var isSaved : Boolean = false

    init{
        if(!token.exists()){
            throw IllegalArgumentException()
        }
    }

    override fun save(fullpath: FileIOToken) : Validated<Throwable, ExportResultToken> {
        if(isSaved){
            throw IllegalArgumentException()
        }

        return Validated.catch {
            fullpath.moveFileToPath(token)
            isSaved = true

            ExportResultToken(fullpath)
        }
    }
}