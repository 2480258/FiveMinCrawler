package fivemin.core.engine

interface ExportData
{
    var isSaved : Boolean

    fun save(token : FileIOToken) : Result<ExportResultToken>
}

data class ExportResultToken(val fullPath : FileIOToken){
}

interface ExportInfo{
    val token : FileIOToken

    fun addSuffix(replacementWithoutExt : String) : ExportInfo
}

interface ExportHandle
{
    val request : ExportInfo
    val data : ExportData
    fun withNewExportInfo(info: ExportInfo) : ExportHandle
}