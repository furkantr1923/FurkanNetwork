package com.furkan.network.network

import com.furkan.network.models.GenelIpDurumu
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.IpAdresi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Genel (public) IP adresini ipify servisinden öğrenir.
 * Yanıt gerçekten bir IP adresi değilse gösterilmez (uydurma veri yok).
 * Gizlilik: istek yalnızca api.ipify.org / api6.ipify.org adreslerine gider.
 */
class GenelIpServisi {

    private val _durum = MutableStateFlow<GenelIpDurumu>(GenelIpDurumu.Bos)
    val durum: StateFlow<GenelIpDurumu> = _durum.asStateFlow()

    private val kilit = Mutex()
    private var sonKimlik: String? = null
    private var sonZaman = 0L

    /**
     * @param agKimligi ağ değiştiyse önbellek atlanır
     * @param zorla kullanıcı "yenile" dediyse true
     */
    suspend fun yenile(agKimligi: String?, zorla: Boolean = false) {
        if (kilit.isLocked) return
        kilit.withLock {
            val simdi = System.currentTimeMillis()
            val onbellekGecerli = _durum.value is GenelIpDurumu.Basarili &&
                agKimligi == sonKimlik && simdi - sonZaman < ONBELLEK_MS
            if (!zorla && onbellekGecerli) return

            _durum.value = GenelIpDurumu.Yukleniyor
            val (v4, v6) = coroutineScope {
                val a = async { sorgula(IPV4_ADRESI, ipv6 = false) }
                val b = async { sorgula(IPV6_ADRESI, ipv6 = true) }
                a.await() to b.await()
            }
            val ipv4 = v4.getOrNull()
            val ipv6 = v6.getOrNull()
            _durum.value = if (ipv4 == null && ipv6 == null) {
                val neden = v4.exceptionOrNull()?.let(HataMesaji::cevir) ?: "Bilinmeyen hata"
                GenelIpDurumu.Hata("Genel IP alınamadı. $neden")
            } else {
                sonKimlik = agKimligi
                sonZaman = System.currentTimeMillis()
                GenelIpDurumu.Basarili(ipv4, ipv6, sonZaman)
            }
        }
    }

    fun sifirla() {
        if (!kilit.isLocked) _durum.value = GenelIpDurumu.Bos
    }

    private suspend fun sorgula(adres: String, ipv6: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val yanit = HttpYardimcisi.metinGetir(adres, ZAMAN_ASIMI_MS).trim()
            val gecerli = if (ipv6) IpAdresi.ipv6Mi(yanit) else IpAdresi.ipv4Mu(yanit)
            if (gecerli) Result.success(yanit)
            else Result.failure(IOException("Beklenmeyen yanıt biçimi"))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }

    private companion object {
        const val IPV4_ADRESI = "https://api.ipify.org"
        const val IPV6_ADRESI = "https://api6.ipify.org"
        const val ZAMAN_ASIMI_MS = 5_000
        const val ONBELLEK_MS = 120_000L
    }
}
