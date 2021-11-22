package core.engine.transaction.finalizeRequest

import core.request.RequesterTask

interface RequestTaskFactory {
    fun create() : RequesterTask
}