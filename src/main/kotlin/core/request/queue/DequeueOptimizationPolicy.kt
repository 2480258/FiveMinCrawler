package core.request.queue

import core.engine.Request
import core.request.PreprocessedRequest

interface DequeueOptimizationPolicy {
    fun getScore(req : PreprocessedRequest<Request>) : Double
}