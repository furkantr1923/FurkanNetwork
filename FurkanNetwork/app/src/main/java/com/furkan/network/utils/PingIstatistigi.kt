package com.furkan.network.utils

import kotlin.math.abs

data class PingIstatistik(
    val gonderilen: Int,
    val basarili: Int,
    val basarisiz: Int,
    val kayipYuzde: Double,
    val enAz: Double?,
    val ortalama: Double?,
    val enCok: Double?,
    /** Ardışık başarılı yanıtlar arasındaki ortalama fark (ms). */
    val jitter: Double?
) {
    companion object {
        val BOS = PingIstatistik(0, 0, 0, 0.0, null, null, null, null)
    }
}

/** Ping örneklerinden (null = yanıt yok) istatistik hesaplar. */
object PingIstatistigi {
    fun hesapla(sureler: List<Double?>): PingIstatistik {
        if (sureler.isEmpty()) return PingIstatistik.BOS
        val basarili = sureler.filterNotNull()
        val gonderilen = sureler.size
        val kayip = (gonderilen - basarili.size) * 100.0 / gonderilen
        val jitter = if (basarili.size >= 2) {
            basarili.zipWithNext { a, b -> abs(b - a) }.average()
        } else null
        return PingIstatistik(
            gonderilen = gonderilen,
            basarili = basarili.size,
            basarisiz = gonderilen - basarili.size,
            kayipYuzde = kayip,
            enAz = basarili.minOrNull(),
            ortalama = if (basarili.isEmpty()) null else basarili.average(),
            enCok = basarili.maxOrNull(),
            jitter = jitter
        )
    }
}
