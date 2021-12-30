package com.fivemin.core.engine

import arrow.core.*
import kotlinx.serialization.Serializable

interface ExportState {
    fun export(): ContinueExportStateInfo

    fun create(token: ExportHandle): PreprocessedExport
}

@Serializable
data class ContinueExportStateInfo constructor(private val exportInfoSet: List<ExportInfo>) {
    val exports = exportInfoSet
}

class PreprocessedExportInfo constructor(val token: FileIOToken)

class PreprocessedExport constructor(val info: PreprocessedExportInfo, val data: ExportData) {
    fun save(): Either<Throwable, ExportResultToken> {
        return Either.catch {
            if (data.isSaved) {
                IllegalStateException().left()
            }

            data.save(info.token)
        }.flatten()
    }
}
