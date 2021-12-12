package core.request

data class HttpRequesterConfig(val config: RequesterConfig, val defaultProfile : RequestHeaderProfile) {
}

class RequesterExtraImpl : RequesterExtra{
    override val dequeueDecision: DequeueDecisionFactory = DefaultRequesterDequeueDecision()
}

class DefaultRequesterDequeueDecision : DequeueDecisionFactory{
    override fun get(): DequeueDecision {
        return DequeueDecision.ALLOW
    }
}

