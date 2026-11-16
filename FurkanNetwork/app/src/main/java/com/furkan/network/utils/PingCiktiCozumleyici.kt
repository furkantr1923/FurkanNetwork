package com.furkan.network.utils

/**
 * Android'in /system/bin/ping çıktısını satır satır çözümler.
 * Örnek satırlar:
 *  "64 bytes from 142.250.187.206: icmp_seq=1 ttl=117 time=12.3 ms"
 *  "From 192.168.1.1 icmp_seq=1 Time to live exceeded"
 *  "From 10.0.0.1: icmp_seq=1 Destination Host Unreachable"
 */
object PingCiktiCozumleyici {

    data class Cozum(
        /** Yankı yanıtı (echo reply) süresi; yalnızca hedef yanıt verdiyse dolu. */
        val sureMs: Double? = null,
        val ttl: Int? = null,
        /** Yankı yanıtını gönderen adres. */
        val yanitVeren: String? = null,
        /** "Time to live exceeded" mesajını gönderen yönlendirici. */
        val ttlAsildiKaynak: String? = null,
        /** "Unreachable" mesajını gönderen adres ve nedeni. */
        val ulasilamazKaynak: String? = null,
        val ulasilamazNeden: String? = null
    )

    private val SURE = Regex("""time[=<]\s*([0-9]+(?:[.,][0-9]+)?)\s*ms""", RegexOption.IGNORE_CASE)
    private val TTL = Regex("""\bttl=(\d+)""", RegexOption.IGNORE_CASE)
    private val YANIT = Regex("""bytes from\s+([^\s(]+)(?:\s+\(([^)]+)\))?""", RegexOption.IGNORE_CASE)
    private val KAYNAK = Regex("""^From\s+([^\s(]+)(?:\s+\(([^)]+)\))?""", RegexOption.IGNORE_CASE)

    fun coz(cikti: String): Cozum {
        var sonuc = Cozum()
        for (hamSatir in cikti.lineSequence()) {
            val satir = hamSatir.trim()
            if (satir.isEmpty()) continue
            val yanit = YANIT.find(satir)
            if (yanit != null && sonuc.yanitVeren == null) {
                val sure = SURE.find(satir)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
                val ttl = TTL.find(satir)?.groupValues?.get(1)?.toIntOrNull()
                sonuc = sonuc.copy(yanitVeren = adresSec(yanit), sureMs = sure, ttl = ttl)
                continue
            }
            val kaynak = KAYNAK.find(satir) ?: continue
            val adres = adresSec(kaynak)
            val kucuk = satir.lowercase()
            when {
                "time to live exceeded" in kucuk || "time exceeded" in kucuk ->
                    if (sonuc.ttlAsildiKaynak == null) sonuc = sonuc.copy(ttlAsildiKaynak = adres)
                "unreachable" in kucuk || "prohibited" in kucuk ->
                    if (sonuc.ulasilamazKaynak == null) {
                        sonuc = sonuc.copy(ulasilamazKaynak = adres, ulasilamazNeden = ulasilamazNedeni(kucuk))
                    }
            }
        }
        return sonuc
    }

    private fun adresSec(m: MatchResult): String {
        val parantez = m.groupValues.getOrNull(2).orEmpty()
        val ham = parantez.ifEmpty { m.groupValues[1] }
        return ham.removeSuffix(":")
    }

    private fun ulasilamazNedeni(kucuk: String): String = when {
        "host unreachable" in kucuk -> "Hedef cihaza ulaşılamıyor"
        "net unreachable" in kucuk || "network unreachable" in kucuk -> "Hedef ağa ulaşılamıyor"
        "port unreachable" in kucuk -> "Hedef port erişilemez"
        "prohibited" in kucuk -> "Yönetsel olarak engellendi (güvenlik duvarı)"
        else -> "Hedefe ulaşılamıyor"
    }

    /**
     * ping komutu bu cihazda çalışamıyorsa true döner
     * (izin yok, komut yok veya seçenek desteklenmiyor).
     */
    fun kullanilamazMi(cikti: String): Boolean {
        val k = cikti.lowercase()
        return listOf(
            "permission denied", "operation not permitted", "not permitted",
            "invalid option", "unknown option", "illegal option", "usage:",
            "not found", "no such file", "socktype: sock_raw", "can't create raw socket"
        ).any { it in k }
    }

    /** Ağ kapalıyken ping'in yazdığı tipik hata. */
    fun agErisilemezMi(cikti: String): Boolean {
        val k = cikti.lowercase()
        return "network is unreachable" in k || "connect: network" in k
    }
}
