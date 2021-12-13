package fivemin.core.engine

import arrow.core.Option

class SessionInfo
constructor(private val finish : FinishObserver,
            val parent : Option<SessionToken>
)
{
    val token : SessionToken = SessionToken.create()
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