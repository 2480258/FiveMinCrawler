package com.fivemin.core.engine

import arrow.core.Option

interface ConfigController {
    fun <T> getSettings (settingName : String) : Option<T>
}