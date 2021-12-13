package fivemin.core.request.srtf

import fivemin.core.engine.RequestToken

data class SRTFDocumentBlock(val token: RequestToken, val bottomMost : RequestToken, val pageName : SRTFPageBlock)