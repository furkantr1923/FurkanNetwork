package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.SslSonucu
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.Bicim
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.HostDogrulayici
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SslDurumu(
    val girdi: String = "",
    val yukleniyor: Boolean = false,
    val sonuc: SslSonucu? = null,
    val hata: String? = null
)

class SslViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _durum = MutableStateFlow(SslDurumu())
    val durum: StateFlow<SslDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    fun girdiDegisti(metin: String) = _durum.update { it.copy(girdi = metin, hata = null) }

    fun incele() {
        val dogrulama = HostDogrulayici.ayikla(_durum.value.girdi)
        if (dogrulama is HostDogrulayici.Sonuc.Gecersiz) {
            _durum.update { it.copy(hata = dogrulama.neden) }
            return
        }
        val g = dogrulama as HostDogrulayici.Sonuc.Gecerli
        val port = g.port ?: 443
        is_?.cancel()
        _durum.update { it.copy(yukleniyor = true, sonuc = null, hata = null) }
        is_ = viewModelScope.launch {
            try {
                val s = k.ssl.incele(g.host, port)
                _durum.update { it.copy(yukleniyor = false, sonuc = s) }
                val yaprak = s.zincir.first()
                val kalanGun = (yaprak.bitis - System.currentTimeMillis()) / 86_400_000L
                val ozet = "${s.protokol} · ${yaprak.yayinciKisa} · $kalanGun gün kaldı"
                val ayrinti = buildString {
                    appendLine("Sunucu: ${s.host}:${s.port}")
                    appendLine("Protokol: ${s.protokol}")
                    appendLine("Şifre takımı: ${s.sifreTakimi}")
                    appendLine("Ad doğrulaması: ${if (s.adDogrulandi) "Uyumlu" else "UYUMSUZ"}")
                    appendLine("Konu: ${yaprak.konu}")
                    appendLine("Yayıncı: ${yaprak.yayinci}")
                    appendLine("Geçerlilik: ${Bicim.gun(yaprak.baslangic)} – ${Bicim.gun(yaprak.bitis)} ($kalanGun gün)")
                    appendLine("Anahtar: ${yaprak.anahtar}, imza: ${yaprak.imzaAlgoritmasi}")
                    appendLine("SHA-256: ${yaprak.sha256ParmakIzi}")
                    appendLine("Zincir uzunluğu: ${s.zincir.size}")
                }
                k.gecmiseEkle(TestTuru.SSL, s.host, ozet, ayrinti)
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
