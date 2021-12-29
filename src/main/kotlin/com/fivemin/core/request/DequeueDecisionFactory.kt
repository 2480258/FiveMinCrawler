package com.fivemin.core.request

interface DequeueDecisionFactory {
    fun get() : DequeueDecision
}

enum class DequeueDecision{
    ALLOW, DELAY, DENY
}