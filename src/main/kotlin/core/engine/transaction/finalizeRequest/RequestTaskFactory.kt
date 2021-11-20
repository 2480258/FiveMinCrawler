package core.engine.transaction.finalizeRequest

interface RequestTaskFactory {
    fun create() : RequestTask
}