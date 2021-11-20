package core.engine

import arrow.core.*

interface SessionRepository {
    fun create(parent : Either<Unit, SessionToken>) : SessionInfo
    fun getDetachables() : Iterable<SessionToken>
}