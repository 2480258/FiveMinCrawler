package fivemin.core.initialize

import fivemin.core.engine.RequesterTaskFactory
import fivemin.core.request.RequesterTaskImpl
import fivemin.core.request.RequestTaskOption
import fivemin.core.request.RequesterTask

class RequesterTaskFactoryImpl(private val op : RequestTaskOption) : RequesterTaskFactory {
    override fun create(): RequesterTask {
        return RequesterTaskImpl(op)
    }
}