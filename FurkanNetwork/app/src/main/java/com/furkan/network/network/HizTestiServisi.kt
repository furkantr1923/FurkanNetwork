package com.furkan.network.network

import com.furkan.network.models.HizOlcumu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed interface HizOlayi {
    data class Ilerleme(val olcum: HizOlcumu) : HizOlayi
    data class Bitti(val olcum: HizOlcumu, val sureSiniriDoldu: Boolean) : HizOlayi
}

/**
 * İsteğe bağlı indirme hızı testi.
 * Uç nokta: https://speed.cloudflare.com/__down?bytes=N
 * (Cloudflare'ın açık kaynak speedtest istemcisinin kullandığı genel test adresi.)
 * Tek bir HTTPS bağlantısı ölçülür; en fazla [SURE_SINIRI_MS] sürer. Yükleme testi yoktur.
 */
class HizTestiServisi {

    fun indir(hedefBayt: Long): Flow<HizOlayi> = flow {
        val baglanti = URL("$UC_NOKTA$hedefBayt").openConnection() as HttpURLConnection
        try {
            baglanti.connectTimeout = 8_000
            baglanti.readTimeout = 10_000
            baglanti.useCaches = false
            baglanti.setRequestProperty("User-Agent", HttpYardimcisi.KULLANICI_AJANI)
            baglanti.setRequestProperty("Accept-Encoding", "identity")

            val baslangic = System.nanoTime()
            val kod = baglanti.responseCode
            if (kod !in 200..299) throw IOException("Test sunucusu HTTP $kod yanıtı verdi")

            val tampon = ByteArray(64 * 1024)
            var toplam = 0L
            var ilkBaytMs: Long? = null
            var sonRapor = System.nanoTime()
            var sonRaporBayt = 0L
            var sureDoldu = false

            baglanti.inputStream.use { girdi ->
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val n = girdi.read(tampon)
                    if (n < 0) break
                    val simdi = System.nanoTime()
                    if (ilkBaytMs == null) ilkBaytMs = (simdi - baslangic) / 1_000_000
                    toplam += n
                    if (simdi - sonRapor >= RAPOR_ARALIGI_NS) {
                        val anlik = mbps(toplam - sonRaporBayt, simdi - sonRapor)
                        emit(HizOlayi.Ilerleme(olcum(toplam, hedefBayt, simdi - baslangic, anlik, ilkBaytMs)))
                        sonRapor = simdi
                        sonRaporBayt = toplam
                    }
                    if ((simdi - baslangic) / 1_000_000 >= SURE_SINIRI_MS) {
                        sureDoldu = true
                        break
                    }
                }
            }
            val bitis = System.nanoTime()
            if (toplam == 0L) throw IOException("Sunucudan veri alınamadı")
            val son = olcum(toplam, hedefBayt, bitis - baslangic, mbps(toplam, bitis - baslangic), ilkBaytMs)
            emit(HizOlayi.Bitti(son, sureDoldu))
        } finally {
            baglanti.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    private fun olcum(bayt: Long, hedef: Long, gecenNs: Long, anlik: Double, ilkBayt: Long?) = HizOlcumu(
        indirilenBayt = bayt,
        hedefBayt = hedef,
        gecenMs = gecenNs / 1_000_000,
        anlikMbps = anlik,
        ortalamaMbps = mbps(bayt, gecenNs),
        ilkBaytMs = ilkBayt
    )

    private fun mbps(bayt: Long, ns: Long): Double =
        if (ns <= 0) 0.0 else bayt * 8.0 / (ns / 1e9) / 1_000_000.0

    companion object {
        const val UC_NOKTA = "https://speed.cloudflare.com/__down?bytes="
        const val SURE_SINIRI_MS = 30_000L
        private const val RAPOR_ARALIGI_NS = 250_000_000L
    }
}
