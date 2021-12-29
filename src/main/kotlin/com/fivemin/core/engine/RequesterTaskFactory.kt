package com.fivemin.core.engine

import com.fivemin.core.request.RequesterTask

interface RequesterTaskFactory {
    fun create() : RequesterTask
}
