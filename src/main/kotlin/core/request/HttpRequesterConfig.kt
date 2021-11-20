package core.request

data class HttpRequesterConfig(val config: RequesterConfig, val defaultProfile : RequestHeaderProfile, val maintain : RequestMaintainerFactory) {
}

data class RequesterExtraImpl(override val dequeueDecision: DequeueDecisionFactory) : RequesterExtra{

}

class DefaultRequesterDequeueDecision : DequeueDecisionFactory{
    override fun get(): DequeueDecision {
        return DequeueDecision.ALLOW
    }
}

