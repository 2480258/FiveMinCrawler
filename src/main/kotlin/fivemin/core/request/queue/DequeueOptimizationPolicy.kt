package fivemin.core.request.queue

import fivemin.core.engine.Request
import fivemin.core.request.PreprocessedRequest

interface DequeueOptimizationPolicy {
    fun getScore(req : PreprocessedRequest<Request>) : Double
}