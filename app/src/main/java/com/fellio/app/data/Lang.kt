package com.fellio.app.data

enum class Lang(val code: String, val tmdb: String, val rtl: Boolean, val native: String) {
    FA("fa", "fa-IR", true, "فارسی"),
    EN("en", "en-US", false, "English"),
    TR("tr", "tr-TR", false, "Türkçe"),
    AR("ar", "ar-SA", true, "العربية");

    companion object {
        fun fromCode(c: String?): Lang = values().firstOrNull { it.code == c } ?: EN
        /** Maps the device locale to a supported language, else English. */
        fun fromDevice(deviceCode: String): Lang = values().firstOrNull { it.code == deviceCode } ?: EN
    }
}
