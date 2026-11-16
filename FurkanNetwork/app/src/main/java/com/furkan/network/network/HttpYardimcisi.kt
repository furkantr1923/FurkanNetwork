package com.furkan.network.network

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/** Küçük HTTP GET yardımcıları (ek kütüphane yok, yalnızca HTTPS). */
object HttpYardimcisi {

    const val KULLANICI_AJANI = "FurkanNetwork/1.0 (Android)"

    /** Kısa bir metin yanıtı alır (ör. genel IP). Yanıt [enFazlaBayt] ile sınırlıdır. */
    fun metinGetir(adres: String, zamanAsimiMs: Int, enFazlaBayt: Int = 2048): String {
        val baglanti = URL(adres).openConnection() as HttpURLConnection
        try {
            baglanti.connectTimeout = zamanAsimiMs
            baglanti.readTimeout = zamanAsimiMs
            baglanti.useCaches = false
            baglanti.setRequestProperty("User-Agent", KULLANICI_AJANI)
            baglanti.setRequestProperty("Accept", "text/plain")
            val kod = baglanti.responseCode
            if (kod !in 200..299) throw IOException("Sunucu HTTP $kod yanıtı verdi")
            return baglanti.inputStream.use { sinirliOku(it, enFazlaBayt) }
        } finally {
            baglanti.disconnect()
        }
    }

    private fun sinirliOku(girdi: InputStream, sinir: Int): String {
        val tampon = ByteArray(sinir)
        var toplam = 0
        while (toplam < sinir) {
            val n = girdi.read(tampon, toplam, sinir - toplam)
            if (n < 0) break
            toplam += n
        }
        return String(tampon, 0, toplam, Charsets.UTF_8)
    }
}
