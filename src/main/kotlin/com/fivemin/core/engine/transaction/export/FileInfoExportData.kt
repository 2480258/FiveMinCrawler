package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import arrow.core.valid
import com.fivemin.core.engine.ExportData
import com.fivemin.core.engine.ExportResultToken
import com.fivemin.core.engine.FileIOToken

class FileInfoExportData (val token : FileIOToken) : ExportData{
    override var isSaved : Boolean = false

    init{
        if(!token.exists()){
            throw IllegalArgumentException()
        }
    }

    override fun save(fullpath: FileIOToken) : Either<Throwable, ExportResultToken> {
        if(isSaved){
            throw IllegalArgumentException()
        }

        return Either.catch {
            fullpath.moveFileToPath(token)
            isSaved = true

            ExportResultToken(fullpath)
        }
    }
}