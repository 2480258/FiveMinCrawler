package fivemin.core.request.cookie

import fivemin.core.engine.PerformedRequesterInfo

data class CookieSyncGradiant(val source : PerformedRequesterInfo, val destination : Iterable<PerformedRequesterInfo>) {

}