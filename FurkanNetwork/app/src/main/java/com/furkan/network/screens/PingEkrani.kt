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
import com.furkan.network.models.PingYontemi
import com.furkan.network.viewmodel.PingViewModel

private val ADET_SECENEKLERI = listOf(5 to "5", 10 to "10", 20 to "20", 0 to "Sürekli")

@Composable
fun PingEkrani(vm: PingViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val renkler = LocalDurumRenkleri.current
    EkranKapanincaDurdur { vm.durdur() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HostAlani(
            deger = d.girdi,
            degisti = vm::girdiDegisti,
            etiket = "Alan adı veya IP adresi",
            ornek = "google.com",
            calistir = vm::baslat,
            etkin = !d.calisiyor,
            hata = d.hata
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Paket:", style = MaterialTheme.typography.labelLarge)
            ADET_SECENEKLERI.forEach { (adet, etiket) ->
                FilterChip(
                    selected = d.adet == adet,
                    onClick = { vm.adetSec(adet) },
                    enabled = !d.calisiyor,
                    label = { Text(etiket) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnaButon(
                "Başlat", Icons.Rounded.PlayArrow, onClick = vm::baslat,
                etkin = !d.calisiyor && d.girdi.isNotBlank(), modifier = Modifier.weight(1f)
            )
            AnaButon(
                "Durdur", Icons.Rounded.Stop, onClick = vm::durdur,
                etkin = d.calisiyor, tehlikeli = true, modifier = Modifier.weight(1f)
            )
        }

        if (d.calisiyor) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        d.yontemNotu?.let { NotKutusu(it, simge = Icons.Rounded.Warning, renk = renkler.uyari) }

        val s = d.istatistik
        if (d.ornekler.isNotEmpty() || d.hedefIp != null) {
            BilgiKarti(baslik = "Sonuçlar", simge = Icons.Rounded.NetworkCheck, eylem = {
                if (d.hedefIp != null) {
                    Text(d.hedefIp ?: "", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                }
            }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OlcumKutusu("Ortalama", Bicim.ms(s.ortalama), Modifier.weight(1f))
                    OlcumKutusu("En az", Bicim.ms(s.enAz), Modifier.weight(1f))
                    OlcumKutusu("En çok", Bicim.ms(s.enCok), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OlcumKutusu(
                        "Paket kaybı", Bicim.yuzde(s.kayipYuzde), Modifier.weight(1f),
                        renk = if (s.kayipYuzde > 0) renkler.hata else renkler.basari
                    )
                    OlcumKutusu("Başarılı", "${s.basarili}", Modifier.weight(1f), renk = renkler.basari)
                    OlcumKutusu("Başarısız", "${s.basarisiz}", Modifier.weight(1f), renk = if (s.basarisiz > 0) renkler.hata else null)
                }
                BilgiSatiri("Jitter", Bicim.ms(s.jitter))
                BilgiSatiri("Yöntem", d.yontem.etiket)
                if (d.ornekler.size >= 2) {
                    CizgiGrafik(d.ornekler.takeLast(60).map { it.sureMs })
                }
                if (d.yontem == PingYontemi.TCP) {
                    Text(
                        "TCP yönteminde süre, 443 numaralı porta bağlantı kurulum süresidir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (d.ornekler.isNotEmpty()) {
            BilgiKarti(baslik = "Son yanıtlar") {
                d.ornekler.takeLast(15).reversed().forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "#${o.sira}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            if (o.sureMs != null) Bicim.ms(o.sureMs) else (o.not ?: "Yanıt yok"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (o.sureMs != null) MaterialTheme.colorScheme.onSurface else renkler.hata,
                            modifier = Modifier.weight(1f)
                        )
                        if (o.ttl != null) {
                            Text("TTL ${o.ttl}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else if (o.sureMs != null && o.not != null) {
                            Text(o.not, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (d.bitti && !d.calisiyor) {
            Text(
                "Test tamamlandı ve geçmişe kaydedildi (ayarlarda kapatılmadıysa).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OlcumKutusu(etiket: String, deger: String, modifier: Modifier = Modifier, renk: androidx.compose.ui.graphics.Color? = null) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(etiket, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                deger,
                style = MaterialTheme.typography.titleMedium,
                color = renk ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
