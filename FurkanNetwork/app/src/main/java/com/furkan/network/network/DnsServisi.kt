package com.furkan.network.network

import android.net.DnsResolver
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import com.furkan.network.models.DnsKayitSonucu
import com.furkan.network.models.DnsSonucu
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.IpAdresi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executor
import kotlin.coroutines.resume

/**
 * DNS A (IPv4) ve AAAA (IPv6) sorgusu.
 * Android 10+ : android.net.DnsResolver ile her kayıt türü ayrı sorgulanır, yanıt kodu (rcode) okunur.
 * Android 8–9 : sistem çözümleyicisi (InetAddress) kullanılır; bu yöntem açıkça belirtilir.
 */
class DnsServisi {

    suspend fun sorgula(alanAdi: String): DnsSonucu {
        val baslangic = System.nanoTime()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            coroutineScope {
                val a = async { kayitSorgula(alanAdi, DnsResolver.TYPE_A) }
                val aaaa = async { kayitSorgula(alanAdi, DnsResolver.TYPE_AAAA) }
                val v4 = a.await()
                val v6 = aaaa.await()
                DnsSonucu(
                    alanAdi = alanAdi,
                    ipv4 = v4,
                    ipv6 = v6,
                    toplamSureMs = (System.nanoTime() - baslangic) / 1_000_000,
                    yontem = "Android DnsResolver — A ve AAAA ayrı sorgulandı"
                )
            }
        } else {
            sistemCozumleyici(alanAdi, baslangic)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun kayitSorgula(alanAdi: String, tur: Int): DnsKayitSonucu {
        val baslangic = System.nanoTime()
        fun gecen() = (System.nanoTime() - baslangic) / 1_000_000
        val sonuc = withTimeoutOrNull(ZAMAN_ASIMI_MS) {
            suspendCancellableCoroutine<DnsKayitSonucu> { devam ->
                val iptal = CancellationSignal()
                devam.invokeOnCancellation { iptal.cancel() }
                val geriCagri = object : DnsResolver.Callback<List<InetAddress>> {
                    override fun onAnswer(answer: List<InetAddress>, rcode: Int) {
                        val adresler = answer.mapNotNull { it.hostAddress?.let(IpAdresi::kapsamsiz) }.distinct()
                        val hata = when {
                            rcode != 0 -> rcodeAciklamasi(rcode)
                            adresler.isEmpty() -> "Bu türde kayıt yok."
                            else -> null
                        }
                        if (devam.isActive) devam.resume(DnsKayitSonucu(adresler, gecen(), hata))
                    }

                    override fun onError(error: DnsResolver.DnsException) {
                        val mesaj = when (error.code) {
                            DnsResolver.ERROR_PARSE -> "DNS yanıtı çözümlenemedi."
                            else -> "DNS sorgusu başarısız. İnternet bağlantınızı kontrol edin."
                        }
                        if (devam.isActive) devam.resume(DnsKayitSonucu(emptyList(), gecen(), mesaj))
                    }
                }
                try {
                    DnsResolver.getInstance().query(
                        null, alanAdi, tur, DnsResolver.FLAG_EMPTY,
                        Executor { it.run() }, iptal, geriCagri
                    )
                } catch (e: Exception) {
                    if (devam.isActive) devam.resume(DnsKayitSonucu(emptyList(), gecen(), HataMesaji.cevir(e)))
                }
            }
        }
        return sonuc ?: DnsKayitSonucu(emptyList(), gecen(), "Zaman aşımı (${ZAMAN_ASIMI_MS / 1000} sn).")
    }

    private suspend fun sistemCozumleyici(alanAdi: String, baslangic: Long): DnsSonucu {
        val sonuc: Result<List<InetAddress>>? = withTimeoutOrNull(ZAMAN_ASIMI_MS) {
            withContext(Dispatchers.IO) {
                try {
                    Result.success(InetAddress.getAllByName(alanAdi).toList())
                } catch (e: UnknownHostException) {
                    Result.failure<List<InetAddress>>(e)
                } catch (e: SecurityException) {
                    Result.failure<List<InetAddress>>(e)
                }
            }
        }
        val sure = (System.nanoTime() - baslangic) / 1_000_000
        val hata: String? = when {
            sonuc == null -> "Zaman aşımı (${ZAMAN_ASIMI_MS / 1000} sn)."
            sonuc.isFailure -> sonuc.exceptionOrNull()?.let(HataMesaji::cevir)
            else -> null
        }
        val liste = sonuc?.getOrNull().orEmpty()
        val v4 = liste.filterIsInstance<Inet4Address>().mapNotNull { it.hostAddress }.distinct()
        val v6 = liste.filterIsInstance<Inet6Address>().mapNotNull { it.hostAddress?.let(IpAdresi::kapsamsiz) }.distinct()
        return DnsSonucu(
            alanAdi = alanAdi,
            ipv4 = DnsKayitSonucu(v4, sure, hata ?: if (v4.isEmpty()) "Bu türde kayıt yok." else null),
            ipv6 = DnsKayitSonucu(v6, sure, hata ?: if (v6.isEmpty()) "IPv6 kaydı yok ya da cihazın IPv6 bağlantısı olmadığı için sistem göstermedi." else null),
            toplamSureMs = sure,
            yontem = "Sistem çözümleyicisi (Android 9 ve altı: A/AAAA ayrı sorgulanamaz)"
        )
    }

    private fun rcodeAciklamasi(rcode: Int): String = when (rcode) {
        1 -> "Sorgu biçimi hatalı (FORMERR)."
        2 -> "DNS sunucusu hata verdi (SERVFAIL)."
        3 -> "Alan adı bulunamadı (NXDOMAIN)."
        4 -> "Sunucu bu sorgu türünü desteklemiyor (NOTIMP)."
        5 -> "DNS sunucusu sorguyu reddetti (REFUSED)."
        else -> "DNS hata kodu: $rcode"
    }

    private companion object {
        const val ZAMAN_ASIMI_MS = 8_000L
    }
}
