package core.engine.transaction.export

import core.engine.*
import core.engine.transaction.ExecuteExportMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExportTransactionMovement<Document : Request>(private val parser: ExportParser, private val state: ExportState) :
    ExecuteExportMovement<Document> {
    override suspend fun move(
        source: SerializeTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Result<ExportTransaction<Document>>> {
        return coroutineScope {
            async {
                try {
                    var ret = parser.parse(source)

                    Result.success(ExportTransactionImpl(source.request, source.tags, saveResult(ret)))
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }
    }

    private fun saveResult(handles: Iterable<ExportHandle>): Iterable<Result<ExportResultToken>> {
        return handles.map { x ->
            state.create(x)
        }.map {
            it.save()
        }
    }
}