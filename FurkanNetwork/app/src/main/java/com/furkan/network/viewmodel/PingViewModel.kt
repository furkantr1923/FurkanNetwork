package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.PingOrnegi
import com.furkan.network.models.PingYontemi
import com.furkan.network.models.TestTuru
import com.furkan.network.network.PingOlayi
import com.furkan.network.utils.Bicim
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.HostDogrulayici
import com.furkan.network.utils.PingIstatistigi
import com.furkan.network.utils.PingIstatistik
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PingDurumu(
    val girdi: String = "",
    /** 0 = sürekli (durdurulana kadar). */
    val adet: Int = 10,
    val calisiyor: Boolean = false,
    val hedefIp: String? = null,
    val yontem: PingYontemi = PingYontemi.ICMP,
    val yontemNotu: String? = null,
    val ornekler: List<PingOrnegi> = emptyList(),
    val istatistik: PingIstatistik = PingIstatistik.BOS,
    val hata: String? = null,
    val bitti: Boolean = false
)

class PingViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _durum = MutableStateFlow(PingDurumu(adet = k.ayarlar.durum.value.pingAdet))
    val durum: StateFlow<PingDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    fun girdiDegisti(metin: String) = _durum.update { it.copy(girdi = metin, hata = null) }

    fun adetSec(adet: Int) {
        if (!_durum.value.calisiyor) _durum.update { it.copy(adet = adet) }
    }

    fun baslat() {
        val dogrulama = HostDogrulayici.ayikla(_durum.value.girdi)
        if (dogrulama is HostDogrulayici.Sonuc.Gecersiz) {
            _durum.update { it.copy(hata = dogrulama.neden) }
            return
        }
        val host = (dogrulama as HostDogrulayici.Sonuc.Gecerli).host
        is_?.cancel()
        _durum.update {
            PingDurumu(girdi = it.girdi, adet = it.adet, calisiyor = true)
        }
        is_ = viewModelScope.launch {
            try {
                k.ping.calistir(host, _durum.value.adet).collect { olay ->
                    when (olay) {
                        is PingOlayi.Hedef -> _durum.update { it.copy(hedefIp = olay.ip) }
                        is PingOlayi.YontemDegisti -> _durum.update {
                            it.copy(yontem = olay.yontem, yontemNotu = olay.aciklama)
                        }
                        is PingOlayi.Ornek -> _durum.update {
                            val yeni = it.ornekler + olay.ornek
                            it.copy(ornekler = yeni, istatistik = PingIstatistigi.hesapla(yeni.map { o -> o.sureMs }))
                        }
                    }
                }
                _durum.update { it.copy(bitti = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _durum.update { it.copy(hata = HataMesaji.cevir(e)) }
            } finally {
                _durum.update { it.copy(calisiyor = false) }
                withContext(NonCancellable) { kaydet(host) }
            }
        }
    }

    fun durdur() {
        is_?.cancel()
    }

    private suspend fun kaydet(host: String) {
        val d = _durum.value
        if (d.ornekler.isEmpty()) return
        val s = d.istatistik
        val ozet = "Ort ${Bicim.ms(s.ortalama)} · kayıp ${Bicim.yuzde(s.kayipYuzde)} · ${s.basarili}/${s.gonderilen}"
        val ayrinti = buildString {
            appendLine("Hedef: $host (${d.hedefIp ?: "?"})")
            appendLine("Yöntem: ${d.yontem.etiket}")
            appendLine("Gönderilen: ${s.gonderilen}, başarılı: ${s.basarili}, başarısız: ${s.basarisiz}")
            appendLine("Paket kaybı: ${Bicim.yuzde(s.kayipYuzde)}")
            appendLine("En az / ortalama / en çok: ${Bicim.ms(s.enAz)} / ${Bicim.ms(s.ortalama)} / ${Bicim.ms(s.enCok)}")
            appendLine("Jitter: ${Bicim.ms(s.jitter)}")
        }
        k.gecmiseEkle(TestTuru.PING, host, ozet, ayrinti)
    }
}
