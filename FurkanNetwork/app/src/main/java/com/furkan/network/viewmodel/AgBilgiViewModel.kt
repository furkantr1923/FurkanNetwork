package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.AgDurumu
import com.furkan.network.models.KablosuzBilgisi
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.Bicim
import com.furkan.network.utils.SinyalYardimcisi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AgBilgiDurumu(
    val ag: AgDurumu,
    val kablosuz: KablosuzBilgisi?,
    val konumIzni: Boolean,
    val konumAcik: Boolean,
    val guncelleme: Long
)

class AgBilgiViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val tetik = MutableStateFlow(0)

    /** Ekran görünürken 3 saniyede bir yenilenir; ekran kapanınca durur (pil dostu). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val durum: StateFlow<AgBilgiDurumu> = tetik.flatMapLatest {
        flow {
            while (true) {
                emit(oku())
                delay(3_000)
            }
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2_000),
            AgBilgiDurumu(k.agDurumu.anlik(), null, k.kablosuz.konumIzniVar(), true, 0L)
        )

    private fun oku() = AgBilgiDurumu(
        ag = k.agDurumu.anlik(),
        kablosuz = k.kablosuz.oku(),
        konumIzni = k.kablosuz.konumIzniVar(),
        konumAcik = k.kablosuz.konumAcik(),
        guncelleme = System.currentTimeMillis()
    )

    fun yenile() {
        tetik.value = tetik.value + 1
    }

    fun gecmiseKaydet() {
        val d = durum.value
        val kb = d.kablosuz
        val ayrinti = buildString {
            appendLine("Bağlantı türü: ${d.ag.tur.etiket}")
            appendLine("İnternet: ${if (d.ag.internetDogrulandi) "Doğrulandı" else "Doğrulanmadı"}")
            appendLine("VPN: ${if (d.ag.vpnAktif) "Etkin" else "Kapalı"}")
            appendLine("Ölçümlü bağlantı: ${if (d.ag.olcumlu) "Evet" else "Hayır"}")
            if (kb != null) {
                appendLine("Wi-Fi: ${if (kb.wifiBagli) "Bağlı" else if (kb.wifiAcik == true) "Açık, bağlı değil" else "Kapalı/bilinmiyor"}")
                kb.ssid?.let { appendLine("SSID: $it") }
                kb.rssi?.let { appendLine("Sinyal: $it dBm (${SinyalYardimcisi.seviyeEtiketi(SinyalYardimcisi.seviye(it))})") }
                kb.baglantiHiziMbps?.let { appendLine("Bağlantı hızı: $it Mbps") }
                kb.frekansMhz?.let { appendLine("Frekans: $it MHz ${SinyalYardimcisi.bant(it).orEmpty()}") }
                appendLine("Mobil veri: ${if (kb.mobilBagli) "Etkin bağlantı" else if (kb.mobilVeriAcik == true) "Açık (beklemede)" else "Kapalı/bilinmiyor"}")
                kb.operator?.let { appendLine("Operatör: $it") }
            }
            appendLine("Kayıt zamanı: ${Bicim.tarih(System.currentTimeMillis())}")
        }
        viewModelScope.launch {
            k.gecmiseEkle(
                TestTuru.AG,
                "Ağ bilgisi (${d.ag.tur.etiket})",
                if (d.ag.internetDogrulandi) "İnternet var" else "İnternet doğrulanmadı",
                ayrinti
            )
        }
    }
}
