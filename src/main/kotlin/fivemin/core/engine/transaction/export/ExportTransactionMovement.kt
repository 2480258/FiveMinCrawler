package fivemin.core.engine.transaction.export

import arrow.core.Validated
import arrow.core.flatMap
import arrow.core.invalid
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.ExecuteExportMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExportTransactionMovement<Document : Request>(private val parser: ExportParser, private val state: ExportState) :
    ExecuteExportMovement<Document> {

    companion object {
        private val logger = LoggerController.getLogger("ExportTransactionMovement")
    }

    override suspend fun move( //TODO ExportTransaction Exception Logging
        source: SerializeTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, ExportTransaction<Document>>> {
        return coroutineScope {
            async {
                try {
                    logger.info(source.request.getDebugInfo() + " < exporting transaction")
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
            var ret = it.save()

            ret.mapLeft { x ->
                logger.info(it.info.token.fileName.name.name + " < not exported due to: " + x.message)
            }

            ret.map { x ->
                logger.info(it.info.token.fileName.name.name + " < exported")
            }
            ret
        }
    }
}