package com.fivemin.core.export

import com.fivemin.core.engine.ExportInfo
import com.fivemin.core.engine.FileIOToken

data class ExportInfoImpl(override val token: FileIOToken) : ExportInfo {
    override fun addSuffix(suffix: String): ExportInfo {
        return ExportInfoImpl(token.addSuffix(suffix))
    }
}
