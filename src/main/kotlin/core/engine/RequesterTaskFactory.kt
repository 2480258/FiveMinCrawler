package core.engine

import core.request.RequesterTask

interface RequesterTaskFactory {
    fun create() : RequesterTask
}
