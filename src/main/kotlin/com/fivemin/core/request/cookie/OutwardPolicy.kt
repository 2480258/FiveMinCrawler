package com.fivemin.core.request.cookie

class OutwardPolicy(val destination: List<CookieRepository>) {
    fun syncTo(source: CookieRepository) {
        destination.forEach {
            it.download(source)
        }
    }
}
