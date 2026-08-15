package com.berat.socialhabitapp.core.util

import java.time.ZoneId
import javax.inject.Inject

class DefaultTimezoneProvider @Inject constructor() : TimezoneProvider {
    override fun getTimezone(): String {
        return ZoneId.systemDefault().id
    }
}
