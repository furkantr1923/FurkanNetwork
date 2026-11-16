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
import com.furkan.network.models.DnsKayitSonucu
import com.furkan.network.viewmodel.DnsViewModel

@Composable
fun DnsEkrani(vm: DnsViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    EkranKapanincaDurdur { vm.iptal() }

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
            etiket = "Alan adı",
            ornek = "google.com",
            calistir = vm::sorgula,
            etkin = !d.yukleniyor,
            hata = d.hata
        )
        AnaButon(
            "Sorgula", Icons.Rounded.Search, onClick = vm::sorgula,
            etkin = !d.yukleniyor && d.girdi.isNotBlank(), modifier = Modifier.fillMaxWidth()
        )
        if (d.yukleniyor) YukleniyorSatiri("A ve AAAA kayıtları sorgulanıyor…")

        val sonuc = d.sonuc
        AnimatedVisibility(visible = sonuc != null) {
            if (sonuc != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BilgiKarti(baslik = sonuc.alanAdi, simge = Icons.Rounded.Dns) {
                        BilgiSatiri("Toplam süre", "${sonuc.toplamSureMs} ms")
                        BilgiSatiri("IPv4 kaydı", "${sonuc.ipv4.adresler.size}")
                        BilgiSatiri("IPv6 kaydı", "${sonuc.ipv6.adresler.size}")
                        Text(sonuc.yontem, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    KayitKarti("IPv4 adresleri (A)", sonuc.ipv4)
                    KayitKarti("IPv6 adresleri (AAAA)", sonuc.ipv6)
                }
            }
        }
    }
}

@Composable
private fun KayitKarti(baslik: String, k: DnsKayitSonucu) {
    BilgiKarti(baslik = baslik, eylem = {
        Text("${k.sureMs} ms", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }) {
        if (k.adresler.isEmpty()) {
            Text(
                k.hata ?: "Kayıt yok.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            k.adresler.forEach { BilgiSatiri("•", it, esGenislik = true, kopyalanabilir = true) }
        }
    }
}
