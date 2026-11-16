package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.DnsSonucu
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.HostDogrulayici
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DnsDurumu(
    val girdi: String = "",
    val yukleniyor: Boolean = false,
    val sonuc: DnsSonucu? = null,
    val hata: String? = null
)

class DnsViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _durum = MutableStateFlow(DnsDurumu())
    val durum: StateFlow<DnsDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    fun girdiDegisti(metin: String) = _durum.update { it.copy(girdi = metin, hata = null) }

    fun sorgula() {
        val dogrulama = HostDogrulayici.ayikla(_durum.value.girdi)
        val alan = when (dogrulama) {
            is HostDogrulayici.Sonuc.Gecersiz -> {
                _durum.update { it.copy(hata = dogrulama.neden) }
                return
            }
            is HostDogrulayici.Sonuc.Gecerli -> {
                if (dogrulama.tur != HostDogrulayici.Tur.ALAN_ADI) {
                    _durum.update {
                        it.copy(hata = "DNS sorgusu için alan adı girin (ör. google.com). IP adresleri için Ping veya Traceroute kullanın.")
                    }
                    return
                }
                dogrulama.host
            }
        }
        is_?.cancel()
        _durum.update { it.copy(yukleniyor = true, hata = null, sonuc = null) }
        is_ = viewModelScope.launch {
            try {
                val sonuc = k.dns.sorgula(alan)
                _durum.update { it.copy(yukleniyor = false, sonuc = sonuc) }
                val ozet = "A: ${sonuc.ipv4.adresler.size} · AAAA: ${sonuc.ipv6.adresler.size} · ${sonuc.toplamSureMs} ms"
                val ayrinti = buildString {
                    appendLine("Alan adı: ${sonuc.alanAdi}")
                    appendLine("Yöntem: ${sonuc.yontem}")
                    appendLine("IPv4 (A) [${sonuc.ipv4.sureMs} ms]: ${sonuc.ipv4.adresler.joinToString().ifEmpty { sonuc.ipv4.hata ?: "—" }}")
                    appendLine("IPv6 (AAAA) [${sonuc.ipv6.sureMs} ms]: ${sonuc.ipv6.adresler.joinToString().ifEmpty { sonuc.ipv6.hata ?: "—" }}")
                    appendLine("Toplam süre: ${sonuc.toplamSureMs} ms")
                }
                k.gecmiseEkle(TestTuru.DNS, sonuc.alanAdi, ozet, ayrinti)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _durum.update { it.copy(yukleniyor = false, hata = HataMesaji.cevir(e)) }
            }
        }
    }

    fun iptal() {
        is_?.cancel()
        _durum.update { it.copy(yukleniyor = false) }
    }
}
