package com.furkan.network.network

import com.furkan.network.models.TraceAtlama
import com.furkan.network.utils.KullaniciHatasi
import com.furkan.network.utils.PingCiktiCozumleyici
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.Inet4Address
import java.net.InetAddress

sealed interface TraceOlayi {
    data class Hedef(val ip: String) : TraceOlayi
    data class Atlama(val atlama: TraceAtlama) : TraceOlayi
    data class Bitti(val ulasildi: Boolean, val aciklama: String) : TraceOlayi
    /** Cihaz/Android route keşfine izin vermiyor; hiçbir atlama uydurulmaz. */
    data class Desteklenmiyor(val aciklama: String) : TraceOlayi
}

/**
 * Temel traceroute: artan TTL değerleriyle sistem ping komutu çalıştırılır.
 * Ara yönlendiricinin "Time to live exceeded" yanıtı okunur.
 * Not: Ara atlamaların süresi ping tarafından raporlanmaz; işlem süresi ölçülür ve "≈" ile gösterilir.
 */
class TracerouteServisi {

    fun calistir(host: String, enFazlaAtlama: Int = 30): Flow<TraceOlayi> = flow {
        val adresler = InetAddress.getAllByName(host)
        val hedef = adresler.firstOrNull { it is Inet4Address }
            ?: throw KullaniciHatasi(
                "Bu hedefin IPv4 adresi yok. Bu sürümde traceroute yalnızca IPv4 için desteklenir."
            )
        val ip = hedef.hostAddress ?: throw KullaniciHatasi("Adres çözümlenemedi.")
        emit(TraceOlayi.Hedef(ip))

        var ardisikYanitsiz = 0
        var enAzBirYanit = false
        for (ttl in 1..enFazlaAtlama) {
            currentCoroutineContext().ensureActive()
            val baslangic = System.nanoTime()
            val sonuc = KomutCalistirici.calistir(
                listOf("ping", "-c", "1", "-W", "2", "-n", "-t", ttl.toString(), ip), 5_000L
            )
            val gecenMs = (System.nanoTime() - baslangic) / 1e6

            if (sonuc == null || PingCiktiCozumleyici.kullanilamazMi(sonuc.cikti)) {
                emit(
                    TraceOlayi.Desteklenmiyor(
                        "Bu cihazda Android, uygulamaların TTL ayarlı ping göndermesine izin vermiyor. " +
                            "Rota keşfi yapılamadı; sahte atlama bilgisi gösterilmez."
                    )
                )
                return@flow
            }
            if (PingCiktiCozumleyici.agErisilemezMi(sonuc.cikti)) {
                emit(TraceOlayi.Bitti(false, "Ağa erişilemiyor. İnternet bağlantınızı kontrol edin."))
                return@flow
            }
            val cozum = PingCiktiCozumleyici.coz(sonuc.cikti)
            when {
                cozum.yanitVeren != null && cozum.sureMs != null -> {
                    emit(TraceOlayi.Atlama(TraceAtlama(ttl, cozum.yanitVeren, cozum.sureMs, true, true, "Hedefe ulaşıldı")))
                    emit(TraceOlayi.Bitti(true, "$ttl atlamada hedefe ulaşıldı."))
                    return@flow
                }
                cozum.ttlAsildiKaynak != null -> {
                    enAzBirYanit = true
                    ardisikYanitsiz = 0
                    emit(TraceOlayi.Atlama(TraceAtlama(ttl, cozum.ttlAsildiKaynak, gecenMs, false, false, null)))
                }
                cozum.ulasilamazKaynak != null -> {
                    emit(TraceOlayi.Atlama(TraceAtlama(ttl, cozum.ulasilamazKaynak, null, false, false, cozum.ulasilamazNeden)))
                    emit(TraceOlayi.Bitti(false, cozum.ulasilamazNeden ?: "Hedefe ulaşılamıyor."))
                    return@flow
                }
                else -> {
                    ardisikYanitsiz++
                    emit(TraceOlayi.Atlama(TraceAtlama(ttl, null, null, false, false, "Yanıt yok (*)")))
                    if (ardisikYanitsiz >= ARDISIK_SINIR) {
                        val aciklama = if (enAzBirYanit) {
                            "$ARDISIK_SINIR ardışık atlamada yanıt alınamadı; keşif durduruldu. " +
                                "Ara yönlendiriciler ICMP yanıtlarını engelliyor olabilir."
                        } else {
                            "Hiçbir yönlendiriciden yanıt alınamadı. Ağınız veya Android, TTL aşımı " +
                                "(ICMP) yanıtlarını engelliyor olabilir; bu durumda rota keşfi mümkün değildir."
                        }
                        emit(TraceOlayi.Bitti(false, aciklama))
                        return@flow
                    }
                }
            }
        }
        emit(TraceOlayi.Bitti(false, "$enFazlaAtlama atlama sınırına ulaşıldı; hedef yanıt vermedi."))
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val ARDISIK_SINIR = 5
    }
}
