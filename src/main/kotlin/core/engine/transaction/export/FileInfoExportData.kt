package core.engine.transaction.export

import core.engine.ExportData
import core.engine.ExportResultToken
import core.engine.FileIOToken

class FileInfoExportData (val token : FileIOToken) : ExportData{
    override var isSaved : Boolean = false

    init{
        if(!token.exists()){
            throw IllegalArgumentException()
        }
    }

    override fun save(fullpath: FileIOToken) : Result<ExportResultToken>{
        if(isSaved){
            throw IllegalArgumentException()
        }

        return try{
            fullpath.moveFileToPath(token)
            isSaved = true

            Result.success(ExportResultToken(fullpath))
        } catch(e : Exception){
            Result.failure(e)
        }
    }
}