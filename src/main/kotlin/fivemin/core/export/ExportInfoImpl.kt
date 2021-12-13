package fivemin.core.export

import fivemin.core.engine.ExportInfo
import fivemin.core.engine.FileIOToken

data class ExportInfoImpl(override val token : FileIOToken) : ExportInfo {
    override fun addSuffix(suffix : String) : ExportInfo{
        return ExportInfoImpl(token.addSuffix(suffix))
    }
}