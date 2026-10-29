package com.fellio.app.data

import java.util.Locale

fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
    val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    val gy2 = if (gm > 2) gy + 1 else gy
    var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
    var jy = -1595 + (33 * (days / 12053))
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + days / 31
        jd = 1 + days % 31
    } else {
        jm = 7 + (days - 186) / 30
        jd = 1 + (days - 186) % 30
    }
    return Triple(jy, jm, jd)
}

/** iso = "yyyy-MM-dd". Returns the Jalali date "yyyy/MM/dd" when [jalali] is true. */
fun formatDate(iso: String, jalali: Boolean): String {
    if (!jalali || iso.length < 10) return iso
    val y = iso.substring(0, 4).toIntOrNull()
    val m = iso.substring(5, 7).toIntOrNull()
    val d = iso.substring(8, 10).toIntOrNull()
    if (y == null || m == null || d == null || m !in 1..12) return iso
    val (jy, jm, jd) = gregorianToJalali(y, m, d)
    return String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd)
}
