package com.fivemin.core.engine

interface FinishObserver {
    fun onStart()
    fun onFinish(token: SessionToken)
    fun onExportableFinish(token: SessionToken)
    fun waitFinish()
}
