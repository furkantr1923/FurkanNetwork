@file:OptIn(ExperimentalMaterial3Api::class)

package com.furkan.network.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.furkan.network.components.*
import com.furkan.network.ui.theme.LocalDurumRenkleri
import com.furkan.network.utils.Bicim
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.furkan.network.network.HizTestiServisi
import com.furkan.network.viewmodel.HizTestiViewModel

private val BOYUTLAR = listOf(10, 25, 50)

@Composable
fun HizTestiEkrani(vm: HizTestiViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val renkler = LocalDurumRenkleri.current
    var olcumluUyari by remember { mutableStateOf(false) }
    EkranKapanincaDurdur { vm.durdur() }

    fun baslatIste() {
        if (vm.olcumluMu()) olcumluUyari = true else vm.baslat()
    }

    if (olcumluUyari) {
        AlertDialog(
            onDismissRequest = { olcumluUyari = false },
            title = { Text("Ölçümlü bağlantı") },
            text = {
                Text(
                    "Mobil veri veya kotalı bir ağdasınız. Test en fazla ${d.boyutMb} MB veri indirir ve kotanızdan düşebilir. Devam edilsin mi?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    olcumluUyari = false
                    vm.baslat()
                }) { Text("Devam et") }
            },
            dismissButton = {
                TextButton(onClick = { olcumluUyari = false }) { Text("Vazgeç") }
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NotKutusu(
            "İsteğe bağlı test. Veri Cloudflare'ın genel hız testi sunucusundan (speed.cloudflare.com) tek bir HTTPS " +
                "bağlantısıyla indirilir; en fazla 30 saniye sürer. Yükleme testi bu sürümde yoktur."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Veri miktarı:", style = MaterialTheme.typography.labelLarge)
            BOYUTLAR.forEach { mb ->
                FilterChip(
                    selected = d.boyutMb == mb,
                    onClick = { vm.boyutSec(mb) },
                    enabled = !d.calisiyor,
                    label = { Text("$mb MB") }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnaButon(
                "Testi başlat", Icons.Rounded.PlayArrow, onClick = { baslatIste() },
                etkin = !d.calisiyor, modifier = Modifier.weight(1f)
            )
            AnaButon(
                "Durdur", Icons.Rounded.Stop, onClick = vm::durdur,
                etkin = d.calisiyor, tehlikeli = true, modifier = Modifier.weight(1f)
            )
        }

        val o = d.olcum
        if (d.calisiyor) {
            val oran = if (o != null && o.hedefBayt > 0) (o.indirilenBayt.toFloat() / o.hedefBayt).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { oran }, modifier = Modifier.fillMaxWidth())
        }
        d.hata?.let { HataKutusu(it) }

        if (o != null) {
            BilgiKarti(baslik = if (d.bitti) "Sonuç" else "Ölçülüyor…", simge = Icons.Rounded.Speed) {
                Text(
                    Bicim.mbps(if (d.bitti) o.ortalamaMbps else o.anlikMbps),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    if (d.bitti) "ortalama indirme hızı" else "anlık hız",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BilgiSatiri("Ortalama", Bicim.mbps(o.ortalamaMbps))
                BilgiSatiri("İndirilen", "${Bicim.bayt(o.indirilenBayt)} / ${Bicim.bayt(o.hedefBayt)}")
                BilgiSatiri("Süre", "${Bicim.sayi(o.gecenMs / 1000.0)} sn")
                o.ilkBaytMs?.let { BilgiSatiri("İlk bayt süresi", "$it ms") }
                if (d.ornekler.size >= 2) {
                    CizgiGrafik(d.ornekler.map { it }, birim = "Mbps", yukseklik = 110.dp)
                }
                if (d.sureSiniri) {
                    Text(
                        "${HizTestiServisi.SURE_SINIRI_MS / 1000} saniyelik süre sınırına ulaşıldı; sonuç o ana kadar indirilen veriye göredir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = renkler.uyari
                    )
                }
            }
        }
        if (d.durduruldu && !d.bitti) {
            Text("Test durduruldu.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "Not: Sonuç cihaz, Wi-Fi sinyali, sunucuya uzaklık ve anlık ağ yoğunluğundan etkilenir; tek bağlantı ölçümü hattınızın tam kapasitesinden düşük çıkabilir.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
