package com.fivemin.core.initialize

import com.fivemin.core.engine.RequesterTaskFactory
import com.fivemin.core.request.RequestTaskOption
import com.fivemin.core.request.RequesterTask
import com.fivemin.core.request.RequesterTaskImpl

class RequesterTaskFactoryImpl(private val op: RequestTaskOption) : RequesterTaskFactory {
    override fun create(): RequesterTask {
        return RequesterTaskImpl(op)
    }
}
