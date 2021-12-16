package fivemin.core.engine

import arrow.core.Validated
import arrow.core.flatten
import arrow.core.invalid
import arrow.core.valid
import kotlinx.serialization.Serializable

interface ExportState {
    fun export() : ContinueExportStateInfo

    fun create(token : ExportHandle) : PreprocessedExport
}

@Serializable
data class ContinueExportStateInfo constructor(private val exportInfoSet : Iterable<ExportInfo>){
    var exports : Set<ExportInfo> = setOf()

    init {
        exports = exportInfoSet.toSet()
    }

}

class PreprocessedExportInfo constructor(val token : FileIOToken){

}

class PreprocessedExport constructor(val info: PreprocessedExportInfo, val data : ExportData){
    fun save() : Validated<Throwable, ExportResultToken> {
        return Validated.catch {
            if(data.isSaved) {
                IllegalStateException().invalid()
            }

            data.save(info.token).toEither()
        }.toEither().flatten().toValidated()
    }
}