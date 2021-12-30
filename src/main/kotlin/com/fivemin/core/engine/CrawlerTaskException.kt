package com.fivemin.core.engine

data class RequestCanceledException(val m: String, val c: Exception) : Exception(m, c)

data class RequestDetachedException(val m: String, val c: Exception) : Exception(m, c)
