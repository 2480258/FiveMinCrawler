package core.request

import core.engine.Request

data class DequeuedRequest(val request : PreprocessedRequest<Request>,
                          val info : DequeuedRequestInfo) {

}

class DequeuedRequestInfo{

}