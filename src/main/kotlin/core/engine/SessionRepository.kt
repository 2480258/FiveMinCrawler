package core.engine

import arrow.core.*

interface SessionRepository {
    fun create(parent : Option<SessionToken>) : SessionInfo
    fun getDetachables() : Iterable<SessionToken>
}