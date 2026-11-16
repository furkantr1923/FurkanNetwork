package com.furkan.network.utils

import java.io.InputStream
import java.security.MessageDigest

/** SHA-256 hesaplama yardımcıları (çevrimdışı). */
object HashYardimcisi {

    private val HEX = "0123456789abcdef".toCharArray()

    fun sha256(bayt: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bayt)

    fun sha256Hex(metin: String): String = hex(sha256(metin.toByteArray(Charsets.UTF_8)))

    /**
     * Akıştaki tüm baytların SHA-256 değerini hesaplar.
     * [kontrol] her blokta çağrılır; iptal için istisna fırlatabilir.
     */
    fun sha256Akis(girdi: InputStream, kontrol: () -> Unit = {}): Pair<String, Long> {
        val ozet = MessageDigest.getInstance("SHA-256")
        val tampon = ByteArray(64 * 1024)
        var toplam = 0L
        while (true) {
            kontrol()
            val n = girdi.read(tampon)
            if (n < 0) break
            ozet.update(tampon, 0, n)
            toplam += n
        }
        return hex(ozet.digest()) to toplam
    }

    fun hex(bayt: ByteArray): String {
        val sb = StringBuilder(bayt.size * 2)
        for (b in bayt) {
            val v = b.toInt() and 0xFF
            sb.append(HEX[v ushr 4]).append(HEX[v and 0x0F])
        }
        return sb.toString()
    }

    /** Sertifika parmak izi biçimi: "AB:CD:..." */
    fun parmakIzi(bayt: ByteArray): String = hex(sha256(bayt)).uppercase().chunked(2).joinToString(":")

    /** Büyük/küçük harf, boşluk ve ':' farklarını yok sayarak karşılaştırır. */
    fun esitMi(a: String, b: String): Boolean {
        val x = normalle(a)
        val y = normalle(b)
        if (x.isEmpty() || y.isEmpty()) return false
        return MessageDigest.isEqual(x.toByteArray(), y.toByteArray())
    }

    private fun normalle(s: String): String = s.filterNot { it.isWhitespace() || it == ':' }.lowercase()
}
