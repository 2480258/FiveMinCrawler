package com.fivemin.core.export

import arrow.core.Option
import com.fivemin.core.engine.*

class ExportStateImpl(private val directIO: DirectIO, private val continueExportStateInfo: Option<ContinueExportStateInfo>) : ExportState {
    private val set: MutableSet<ExportInfo>
    private val DUP_STRING: String = " - (Dup)"

    private val lock: Any = Any()

    private val directoryIOToken: DirectoryIOToken = directIO.getToken(UsingPath.EXPORT)

    init {
        set = continueExportStateInfo.fold({ mutableSetOf<ExportInfo>() }, { x -> x.exports.toMutableSet() })
    }

    override fun export(): ContinueExportStateInfo {
        return ContinueExportStateInfo(set.toList())
    }

    override fun create(token: ExportHandle): PreprocessedExport {
        return createInternal(token)
    }

    private fun createInternal(handle: ExportHandle): PreprocessedExport {
        synchronized(lock) {
            if (checkFilenameDuplicate(handle.request)) {
                return createInternal(handle.withNewExportInfo(handle.request.addSuffix(DUP_STRING)))
            }

            set.add(handle.request)

            return PreprocessedExport(getInfo(handle), handle.data)
        }
    }

    private fun checkFilenameDuplicate(info: ExportInfo): Boolean {
        return set.any {
            it.token.fileName == info.token.fileName
        }
    }

    private fun getInfo(handle: ExportHandle): PreprocessedExportInfo {
        return PreprocessedExportInfo(handle.request.token)
    }
}