package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.TestTuru
import com.furkan.network.models.TraceAtlama
import com.furkan.network.network.TraceOlayi
import com.furkan.network.utils.Bicim
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.HostDogrulayici
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TraceDurumu(
    val girdi: String = "",
    val calisiyor: Boolean = false,
    val hedefIp: String? = null,
    val atlamalar: List<TraceAtlama> = emptyList(),
    val sonucMesaji: String? = null,
    val ulasildi: Boolean = false,
    val desteklenmiyor: String? = null,
    val hata: String? = null
)

class TracerouteViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _durum = MutableStateFlow(TraceDurumu())
    val durum: StateFlow<TraceDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    fun girdiDegisti(metin: String) = _durum.update { it.copy(girdi = metin, hata = null) }

    fun baslat() {
        val dogrulama = HostDogrulayici.ayikla(_durum.value.girdi)
        if (dogrulama is HostDogrulayici.Sonuc.Gecersiz) {
            _durum.update { it.copy(hata = dogrulama.neden) }
            return
        }
        val host = (dogrulama as HostDogrulayici.Sonuc.Gecerli).host
        is_?.cancel()
        _durum.update { TraceDurumu(girdi = it.girdi, calisiyor = true) }
        is_ = viewModelScope.launch {
            try {
                k.traceroute.calistir(host).collect { olay ->
                    when (olay) {
                        is TraceOlayi.Hedef -> _durum.update { it.copy(hedefIp = olay.ip) }
                        is TraceOlayi.Atlama -> _durum.update { it.copy(atlamalar = it.atlamalar + olay.atlama) }
                        is TraceOlayi.Bitti -> _durum.update {
                            it.copy(sonucMesaji = olay.aciklama, ulasildi = olay.ulasildi)
                        }
                        is TraceOlayi.Desteklenmiyor -> _durum.update { it.copy(desteklenmiyor = olay.aciklama) }
                    }
                }
            } catch (e: CancellationException) {
                _durum.update { if (it.sonucMesaji == null) it.copy(sonucMesaji = "Kullanıcı tarafından durduruldu.") else it }
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
        if (d.atlamalar.isEmpty()) return
        val yanitlayan = d.atlamalar.count { it.adres != null }
        val ozet = "${d.atlamalar.size} atlama · $yanitlayan yanıt · " + if (d.ulasildi) "hedefe ulaşıldı" else "hedefe ulaşılamadı"
        val ayrinti = buildString {
            appendLine("Hedef: $host (${d.hedefIp ?: "?"})")
            d.atlamalar.forEach { a ->
                val sure = when {
                    a.sureMs == null -> "*"
                    a.kesinSure -> Bicim.ms(a.sureMs)
                    else -> "≈" + Bicim.ms(a.sureMs)
                }
                appendLine("${a.sira.toString().padStart(2)}  ${(a.adres ?: "*").padEnd(40)} $sure ${a.not.orEmpty()}")
            }
            d.sonucMesaji?.let { appendLine("Sonuç: $it") }
        }
        k.gecmiseEkle(TestTuru.TRACEROUTE, host, ozet, ayrinti)
    }
}
