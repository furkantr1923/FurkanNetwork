package com.furkan.network.utils

/** Wi-Fi sinyal ve frekans yorumlama (saf hesaplama). */
object SinyalYardimcisi {

    /** RSSI (dBm) → 0..4 seviye. Geçersiz değerde null. */
    fun seviye(rssi: Int?): Int? {
        if (rssi == null || rssi <= -127 || rssi >= 0) return null
        return when {
            rssi >= -55 -> 4
            rssi >= -66 -> 3
            rssi >= -77 -> 2
            rssi >= -88 -> 1
            else -> 0
        }
    }

    fun seviyeEtiketi(seviye: Int?): String = when (seviye) {
        4 -> "Mükemmel"
        3 -> "İyi"
        2 -> "Orta"
        1 -> "Zayıf"
        0 -> "Çok zayıf"
        else -> "Bilinmiyor"
    }

    fun bant(frekansMhz: Int?): String? = when (frekansMhz) {
        null -> null
        in 2400..2500 -> "2,4 GHz"
        in 4900..5899 -> "5 GHz"
        in 5925..7125 -> "6 GHz"
        else -> null
    }

    fun kanal(frekansMhz: Int?): Int? = when (frekansMhz) {
        null -> null
        2484 -> 14
        in 2412..2472 -> (frekansMhz - 2407) / 5
        in 5000..5899 -> (frekansMhz - 5000) / 5
        in 5955..7115 -> (frekansMhz - 5950) / 5
        else -> null
    }
}
