package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.HizOlcumu
import com.furkan.network.models.TestTuru
import com.furkan.network.network.HizOlayi
import com.furkan.network.utils.Bicim
import com.furkan.network.utils.HataMesaji
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HizDurumu(
    val boyutMb: Int = 25,
    val calisiyor: Boolean = false,
    val olcum: HizOlcumu? = null,
    val ornekler: List<Double> = emptyList(),
    val bitti: Boolean = false,
    val sureSiniri: Boolean = false,
    val durduruldu: Boolean = false,
    val hata: String? = null
)

class HizTestiViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _durum = MutableStateFlow(HizDurumu())
    val durum: StateFlow<HizDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    /** Ölçümlü (ör. mobil veri) bağlantıda kullanıcı uyarılır. */
    fun olcumluMu(): Boolean = k.agDurumu.anlik().olcumlu

    fun internetVarMi(): Boolean = k.agDurumu.anlik().bagli

    fun boyutSec(mb: Int) {
        if (!_durum.value.calisiyor) _durum.update { it.copy(boyutMb = mb) }
    }

    fun baslat() {
        is_?.cancel()
        val mb = _durum.value.boyutMb
        _durum.update { HizDurumu(boyutMb = mb, calisiyor = true) }
        is_ = viewModelScope.launch {
            try {
                k.hizTesti.indir(mb * 1_000_000L).collect { olay ->
                    when (olay) {
                        is HizOlayi.Ilerleme -> _durum.update {
                            it.copy(olcum = olay.olcum, ornekler = (it.ornekler + olay.olcum.anlikMbps).takeLast(120))
                        }
                        is HizOlayi.Bitti -> {
                            _durum.update { it.copy(olcum = olay.olcum, bitti = true, sureSiniri = olay.sureSiniriDoldu) }
                            kaydet(olay.olcum, olay.sureSiniriDoldu)
                        }
                    }
                }
            } catch (e: CancellationException) {
                _durum.update { it.copy(durduruldu = true) }
                throw e
            } catch (e: Exception) {
                _durum.update { it.copy(hata = HataMesaji.cevir(e)) }
            } finally {
                _durum.update { it.copy(calisiyor = false) }
            }
        }
    }

    fun durdur() {
        is_?.cancel()
    }

    private suspend fun kaydet(o: HizOlcumu, sinir: Boolean) {
        val ozet = "Ort ${Bicim.mbps(o.ortalamaMbps)} · ${Bicim.bayt(o.indirilenBayt)} / ${Bicim.sayi(o.gecenMs / 1000.0)} sn"
        val ayrinti = buildString {
            appendLine("Sunucu: speed.cloudflare.com (tek HTTPS bağlantısı)")
            appendLine("İndirilen: ${Bicim.bayt(o.indirilenBayt)} / hedef ${Bicim.bayt(o.hedefBayt)}")
            appendLine("Süre: ${Bicim.sayi(o.gecenMs / 1000.0)} sn${if (sinir) " (30 sn sınırı)" else ""}")
            appendLine("Ortalama hız: ${Bicim.mbps(o.ortalamaMbps)}")
            o.ilkBaytMs?.let { appendLine("İlk bayt süresi: $it ms") }
        }
        k.gecmiseEkle(TestTuru.HIZ, "İndirme testi (${Bicim.bayt(o.hedefBayt)})", ozet, ayrinti)
    }
}
