package fivemin.core.engine

import arrow.core.Either

interface ExportData
{
    var isSaved : Boolean

    fun save(token : FileIOToken) : Either<Throwable, ExportResultToken>
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