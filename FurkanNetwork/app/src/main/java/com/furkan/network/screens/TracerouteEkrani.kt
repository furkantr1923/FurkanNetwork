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
import com.furkan.network.viewmodel.TracerouteViewModel

@Composable
fun TracerouteEkrani(vm: TracerouteViewModel = viewModel()) {
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
            etiket = "Hedef alan adı veya IPv4",
            ornek = "google.com",
            calistir = vm::baslat,
            etkin = !d.calisiyor,
            hata = d.hata
        )
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
        NotKutusu(
            "Her atlama için TTL değeri artırılarak sistem ping komutu çalıştırılır (en fazla 30 atlama). " +
                "Ara yönlendirici süreleri ping tarafından raporlanmadığı için “≈” ile gösterilen değer işlem süresini de içerir."
        )
        if (d.calisiyor) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        d.desteklenmiyor?.let { HataKutusu(it) }

        if (d.atlamalar.isNotEmpty()) {
            BilgiKarti(baslik = "Rota", simge = Icons.Rounded.Timeline, eylem = {
                d.hedefIp?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                }
            }) {
                d.atlamalar.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            a.sira.toString().padStart(2),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(32.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                a.adres ?: "*  *  *",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = when {
                                    a.hedef -> renkler.basari
                                    a.adres == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            a.not?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(
                            when {
                                a.sureMs == null -> "—"
                                a.kesinSure -> Bicim.ms(a.sureMs)
                                else -> "≈ " + Bicim.ms(a.sureMs)
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        d.sonucMesaji?.let {
            if (d.ulasildi) NotKutusu(it, simge = Icons.Rounded.CheckCircle, renk = renkler.basari)
            else NotKutusu(it, simge = Icons.Rounded.Info, renk = renkler.uyari)
        }
    }
}
