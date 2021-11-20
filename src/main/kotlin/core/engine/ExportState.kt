package core.engine

interface ExportState {
    fun export() : ContinueExportStateInfo

    fun create(token : ExportHandle) : PreprocessedExport
}

class ContinueExportStateInfo constructor(private val exportInfoSet : Iterable<ExportInfo>){
    val exports : Set<ExportInfo>

    init {
        exports = exportInfoSet.toSet()
    }

}

class PreprocessedExportInfo constructor(val token : FileIOToken){

}

class PreprocessedExport constructor(val info: PreprocessedExportInfo, val data : ExportData){
    fun save() : Result<ExportResultToken>{
        return try{
            if(data.isSaved){
                throw IllegalStateException()
            }
            data.save(info.token)
        } catch (e : Exception){
            Result.failure(e)
        }
    }
}