package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.AgDurumu
import com.furkan.network.models.Ayarlar
import com.furkan.network.models.GenelIpDurumu
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.Bicim
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Panel ve IP Bilgisi ekranlarının ortak ViewModel'i. */
class PanelViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner

    val agDurumu: StateFlow<AgDurumu> = k.agDurumu.akis()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), k.agDurumu.anlik())

    val genelIp: StateFlow<GenelIpDurumu> = k.genelIp.durum
    val ayarlar: StateFlow<Ayarlar> = k.ayarlar.durum

    /** Ağ değiştiğinde ekrandan çağrılır; ayar açıksa genel IP'yi günceller. */
    fun agDegisti(durum: AgDurumu) {
        if (!durum.bagli) {
            k.genelIp.sifirla()
            return
        }
        if (durum.internetDogrulandi && k.ayarlar.durum.value.genelIpOtomatik) {
            viewModelScope.launch { k.genelIp.yenile(durum.kimlik) }
        }
    }

    fun genelIpYenile() {
        viewModelScope.launch { k.genelIp.yenile(agDurumu.value.kimlik, zorla = true) }
    }

    fun gecmiseKaydet() {
        val d = agDurumu.value
        val ip = genelIp.value
        val genel = (ip as? GenelIpDurumu.Basarili)
        val ayrinti = buildString {
            appendLine("Bağlantı türü: ${d.tur.etiket}${if (d.vpnAktif) " (VPN etkin)" else ""}")
            appendLine("İnternet: ${if (d.internetDogrulandi) "Doğrulandı" else "Doğrulanmadı"}")
            appendLine("Yerel IPv4: ${d.yerelIpv4.joinToString().ifEmpty { "—" }}")
            appendLine("IPv6: ${d.yerelIpv6.joinToString().ifEmpty { "—" }}")
            appendLine("Genel IPv4: ${genel?.ipv4 ?: "—"}")
            appendLine("Genel IPv6: ${genel?.ipv6 ?: "—"}")
            appendLine("Ağ geçidi: ${d.agGecidi ?: "—"}")
            appendLine("DNS sunucuları: ${d.dnsSunuculari.joinToString().ifEmpty { "—" }}")
            d.ozelDns?.let { appendLine("Özel DNS: $it") }
            appendLine("Kayıt zamanı: ${Bicim.tarih(System.currentTimeMillis())}")
        }
        viewModelScope.launch {
            k.gecmiseEkle(
                TestTuru.IP,
                "IP bilgisi (${d.tur.etiket})",
                "Yerel ${d.yerelIpv4.firstOrNull() ?: "—"} · Genel ${genel?.ipv4 ?: genel?.ipv6 ?: "—"}",
                ayrinti
            )
        }
    }
}
