package com.furkan.network.network

import com.furkan.network.models.PingOrnegi
import com.furkan.network.models.PingYontemi
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.KullaniciHatasi
import com.furkan.network.utils.PingCiktiCozumleyici
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.ConnectException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

sealed interface PingOlayi {
    data class Hedef(val ip: String, val ipv6: Boolean) : PingOlayi
    data class YontemDegisti(val yontem: PingYontemi, val aciklama: String) : PingOlayi
    data class Ornek(val ornek: PingOrnegi) : PingOlayi
}

/**
 * Ping testi.
 * Önce Android'in sistem ping komutu (ICMP) denenir. Cihaz buna izin vermiyorsa
 * TCP 443 bağlantı süresi ölçülür ve bu durum kullanıcıya AÇIKÇA bildirilir.
 */
class PingServisi {

    /** @param adet 0 ise kullanıcı durdurana kadar (en fazla [SUREKLI_SINIR]) çalışır. */
    fun calistir(host: String, adet: Int, aralikMs: Long = 1_000L): Flow<PingOlayi> = flow {
        val adres = cozumle(host)
        val ip = adres.hostAddress ?: throw KullaniciHatasi("Adres çözümlenemedi.")
        val ipv6 = adres is Inet6Address
        emit(PingOlayi.Hedef(ip, ipv6))

        var yontem = PingYontemi.ICMP
        val sinir = if (adet <= 0) SUREKLI_SINIR else adet
        var sira = 0
        while (sira < sinir) {
            currentCoroutineContext().ensureActive()
            sira++
            val baslangic = System.nanoTime()
            val ornek: PingOrnegi = if (yontem == PingYontemi.ICMP) {
                val icmp = icmpPing(sira, ip, ipv6)
                if (icmp != null) {
                    icmp
                } else {
                    yontem = PingYontemi.TCP
                    emit(
                        PingOlayi.YontemDegisti(
                            PingYontemi.TCP,
                            "Bu cihaz uygulamaların ICMP ping göndermesine izin vermiyor. " +
                                "Bunun yerine TCP 443 bağlantı kurulum süresi ölçülüyor; değerler ICMP ping ile birebir aynı değildir."
                        )
                    )
                    tcpPing(sira, adres)
                }
            } else {
                tcpPing(sira, adres)
            }
            emit(PingOlayi.Ornek(ornek))
            if (sira < sinir) {
                val gecen = (System.nanoTime() - baslangic) / 1_000_000
                delay((aralikMs - gecen).coerceAtLeast(200L))
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun cozumle(host: String): InetAddress {
        val adresler = InetAddress.getAllByName(host)
        if (adresler.isEmpty()) throw KullaniciHatasi("Adres için IP bulunamadı.")
        return adresler.firstOrNull { it is Inet4Address } ?: adresler.first()
    }

    /** ICMP kullanılamıyorsa null; yanıt yoksa sureMs=null olan örnek döner. */
    private suspend fun icmpPing(sira: Int, ip: String, ipv6: Boolean): PingOrnegi? {
        val komut = listOf(if (ipv6) "ping6" else "ping", "-c", "1", "-W", "2", "-n", ip)
        val sonuc = KomutCalistirici.calistir(komut, 5_000L) ?: return null
        if (sonuc.zamanAsimi) return PingOrnegi(sira, null, null, "Zaman aşımı")
        val cozum = PingCiktiCozumleyici.coz(sonuc.cikti)
        return when {
            cozum.sureMs != null -> PingOrnegi(sira, cozum.sureMs, cozum.ttl, null)
            PingCiktiCozumleyici.kullanilamazMi(sonuc.cikti) -> null
            PingCiktiCozumleyici.agErisilemezMi(sonuc.cikti) -> PingOrnegi(sira, null, null, "Ağa erişilemiyor")
            cozum.ulasilamazKaynak != null -> PingOrnegi(sira, null, null, cozum.ulasilamazNeden)
            sonuc.cikisKodu == 1 -> PingOrnegi(sira, null, null, "Yanıt yok (zaman aşımı)")
            sonuc.cikisKodu == 0 -> PingOrnegi(sira, null, null, "Yanıt süresi okunamadı")
            // Beklenmeyen çıktı + hata kodu: ICMP'yi bu cihazda güvenilir sayma.
            else -> null
        }
    }

    private fun tcpPing(sira: Int, adres: InetAddress): PingOrnegi {
        val baslangic = System.nanoTime()
        fun gecen() = (System.nanoTime() - baslangic) / 1e6
        return try {
            Socket().use { it.connect(InetSocketAddress(adres, 443), 2_000) }
            PingOrnegi(sira, gecen(), null, null)
        } catch (e: SocketTimeoutException) {
            PingOrnegi(sira, null, null, "Zaman aşımı")
        } catch (e: ConnectException) {
            // "Connection refused" = hedef RST ile yanıt verdi, yani erişilebilir.
            if (e.message?.contains("refused", ignoreCase = true) == true) {
                PingOrnegi(sira, gecen(), null, "Port kapalı (RST yanıtı)")
            } else {
                PingOrnegi(sira, null, null, HataMesaji.cevir(e))
            }
        } catch (e: IOException) {
            PingOrnegi(sira, null, null, HataMesaji.cevir(e))
        }
    }

    companion object {
        const val SUREKLI_SINIR = 1_000
    }
}
