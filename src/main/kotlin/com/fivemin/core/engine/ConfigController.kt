package com.fivemin.core.engine

import arrow.core.Option

interface ConfigController {
    fun getSettings(settingName: String): Option<String>
}
