package core.engine

import arrow.core.Either
import arrow.core.Option
import core.engine.DetachableState
import core.engine.FinishObserver
import core.engine.ProgressState
import core.engine.SessionToken

class SessionInfo
constructor(private val finish : FinishObserver,
            val token : SessionToken,
            val parent : Either<Unit, SessionToken>
)
{
    val isDetachable : DetachableState
    get() {return detachable}
    private var detachable = DetachableState.NOTMODIFIED
    private var progress = ProgressState.STARTED
    init
    {
        finish.onStart()
    }

    fun finish() {
        if(progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        progress = ProgressState.FINISHED

        if(detachable == DetachableState.WANT){
            finish.onExportableFinish(token)
        }
        else {
            finish.onFinish(token)
        }
    }

    fun error()
    {
        if(progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        progress = ProgressState.ERROR
        finish.onFinish(token)
    }

    fun setDetachable()
    {
        if(progress != ProgressState.STARTED){
            throw IllegalStateException()
        }

        detachable = DetachableState.WANT
    }

    fun setNonDetachable(){

        if(progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }

        detachable = DetachableState.HATE
    }
}