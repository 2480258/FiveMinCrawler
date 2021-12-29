package com.fivemin.core.request.queue

import com.fivemin.core.engine.Request
import com.fivemin.core.request.PreprocessedRequest

interface DequeueOptimizationPolicy {
    fun getScore(req : PreprocessedRequest<Request>) : Double
}