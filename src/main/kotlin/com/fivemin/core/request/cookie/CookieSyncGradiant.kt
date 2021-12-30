package com.fivemin.core.request.cookie

import com.fivemin.core.engine.PerformedRequesterInfo

data class CookieSyncGradiant(val source: PerformedRequesterInfo, val destination: Iterable<PerformedRequesterInfo>)
