package core.request

import core.engine.Request

interface DequeuedRequest {
    val request : PreprocessedRequest<Request>
    val info : DequeuedRequestInfo
}

class DequeuedRequestInfo{

}