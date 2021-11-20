package core.request.cookie

import core.engine.PerformedRequesterInfo

data class CookieSyncGradiant(val source : PerformedRequesterInfo, val destination : Iterable<PerformedRequesterInfo>) {

}