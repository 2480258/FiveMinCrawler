package core.initialize

import core.engine.RequesterTaskFactory
import core.request.RequesterTaskImpl
import core.request.RequestTaskOption
import core.request.RequesterTask

class RequesterTaskFactoryImpl(private val op : RequestTaskOption) : RequesterTaskFactory {
    override fun create(): RequesterTask {
        return RequesterTaskImpl(op)
    }
}