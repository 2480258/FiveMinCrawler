package core.request.srtf

import core.engine.RequestToken

data class SRTFDocumentBlock(val token: RequestToken, val bottomMost : RequestToken, val pageName : SRTFPageBlock)