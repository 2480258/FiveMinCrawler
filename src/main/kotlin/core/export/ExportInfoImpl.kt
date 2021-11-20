package core.export

import core.engine.ExportInfo
import core.engine.FileIOToken

data class ExportInfoImpl(override val token : FileIOToken) : ExportInfo {
    override fun addSuffix(suffix : String) : ExportInfo{
        return ExportInfoImpl(token.addSuffix(suffix))
    }
}