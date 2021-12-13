package fivemin.core.engine

import fivemin.core.request.RequesterTask

interface RequesterTaskFactory {
    fun create() : RequesterTask
}
