package com.fivemin.core.request.srtf

import com.fivemin.core.engine.RequestToken

data class SRTFDocumentBlock(val token: RequestToken, val bottomMost : RequestToken, val pageName : SRTFPageBlock)