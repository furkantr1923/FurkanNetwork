package com.furkan.network.utils

/**
 * IPv4 / IPv6 metin doğrulama ve dönüştürme yardımcıları.
 * Saf Kotlin'dir; ağ erişimi yapmaz, birim testlerle sınanır.
 */
object IpAdresi {

    /** "192.168.1.10" → 3232235786. Geçersizse null. */
    fun ipv4Ayristir(metin: String): Long? {
        val parcalar = metin.split('.')
        if (parcalar.size != 4) return null
        var sonuc = 0L
        for (p in parcalar) {
            if (p.isEmpty() || p.length > 3 || !p.all { it in '0'..'9' }) return null
            val deger = p.toInt()
            if (deger > 255) return null
            sonuc = (sonuc shl 8) or deger.toLong()
        }
        return sonuc
    }

    fun ipv4Mu(metin: String): Boolean = ipv4Ayristir(metin) != null

    fun ipv4Metni(deger: Long): String {
        val d = deger and 0xFFFFFFFFL
        return "${(d shr 24) and 0xFF}.${(d shr 16) and 0xFF}.${(d shr 8) and 0xFF}.${d and 0xFF}"
    }

    /** IPv4 değerini sekizli gruplar halinde ikili gösterime çevirir. */
    fun ipv4Ikili(deger: Long): String {
        val d = deger and 0xFFFFFFFFL
        return (3 downTo 0).joinToString(".") { i ->
            ((d shr (i * 8)) and 0xFF).toString(2).padStart(8, '0')
        }
    }

    /** IPv6 metnini doğrular ("::1", "2001:db8::1", "::ffff:1.2.3.4", "fe80::1%wlan0"). */
    fun ipv6Mi(girdi: String): Boolean {
        var s = girdi.substringBefore('%')
        if (s.length < 2 || s.length > 45) return false
        if (!s.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' || it == ':' || it == '.' }) return false
        if (!s.contains(':')) return false
        val ciftSayisi = s.windowed(2).count { it == "::" }
        if (ciftSayisi > 1) return false
        if ('.' in s) {
            val son = s.substringAfterLast(':')
            if (ipv4Ayristir(son) == null) return false
            s = s.substringBeforeLast(':') + ":0:0"
        }
        return if (ciftSayisi == 1) {
            val sol = s.substringBefore("::")
            val sag = s.substringAfter("::")
            val solGruplar = if (sol.isEmpty()) emptyList() else sol.split(':')
            val sagGruplar = if (sag.isEmpty()) emptyList() else sag.split(':')
            (solGruplar + sagGruplar).all(::grupGecerli) && solGruplar.size + sagGruplar.size <= 7
        } else {
            val gruplar = s.split(':')
            gruplar.size == 8 && gruplar.all(::grupGecerli)
        }
    }

    private fun grupGecerli(g: String): Boolean =
        g.length in 1..4 && g.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }

    /** IPv6 adresinden kapsam (%wlan0) ekini kaldırır. */
    fun kapsamsiz(adres: String): String = adres.substringBefore('%')

    /** fe80::/10 bağlantı-yerel IPv6 mı? */
    fun ipv6BaglantiYerelMi(adres: String): Boolean {
        val k = adres.lowercase()
        return k.startsWith("fe8") || k.startsWith("fe9") || k.startsWith("fea") || k.startsWith("feb")
    }
}
