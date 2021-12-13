package fivemin.core.request

import fivemin.core.engine.Request

data class DequeuedRequest(val request : PreprocessedRequest<Request>,
                          val info : DequeuedRequestInfo) {

}

class DequeuedRequestInfo{

}