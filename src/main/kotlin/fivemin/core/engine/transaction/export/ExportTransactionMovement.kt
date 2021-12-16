package fivemin.core.engine.transaction.export

import arrow.core.Validated
import arrow.core.invalid
import fivemin.core.engine.*
import fivemin.core.engine.transaction.ExecuteExportMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExportTransactionMovement<Document : Request>(private val parser: ExportParser, private val state: ExportState) :
    ExecuteExportMovement<Document> {
    override suspend fun move(
        source: SerializeTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, ExportTransaction<Document>>> {
        return coroutineScope {
            async {
                try {
                    var ret = parser.parse(source)

                    Validated.catch {
                        ExportTransactionImpl(source.request, source.tags, saveResult(ret))
                    }
                } catch (e: Exception) {
                    e.invalid()
                }
            }
        }
    }

    private fun saveResult(handles: Iterable<ExportHandle>): Iterable<Validated<Throwable, ExportResultToken>> {
        return handles.map { x ->
            state.create(x)
        }.map {
            it.save()
        }
    }
}