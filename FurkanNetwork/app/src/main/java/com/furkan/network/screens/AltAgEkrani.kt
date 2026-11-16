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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.furkan.network.utils.AltAgHesaplayici
import com.furkan.network.viewmodel.AltAgViewModel

@Composable
fun AltAgEkrani(vm: AltAgViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val sayiKlavyesi = KeyboardOptions(keyboardType = KeyboardType.Uri, autoCorrectEnabled = false)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = d.ip,
            onValueChange = vm::ipDegisti,
            label = { Text("IPv4 adresi (CIDR olabilir)") },
            placeholder = { Text("192.168.1.10/24") },
            singleLine = true,
            keyboardOptions = sayiKlavyesi,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = d.maske,
            onValueChange = vm::maskeDegisti,
            label = { Text("Maske veya önek (CIDR yoksa)") },
            placeholder = { Text("255.255.255.0 ya da 24") },
            singleLine = true,
            enabled = !d.ip.contains('/'),
            keyboardOptions = sayiKlavyesi,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnaButon("Hesapla", Icons.Rounded.Calculate, onClick = vm::hesapla, modifier = Modifier.weight(1f))
            IkincilButon("Örnek", Icons.Rounded.Lightbulb, onClick = vm::ornekYukle, modifier = Modifier.weight(1f))
        }

        when (val c = d.cikti) {
            is AltAgHesaplayici.Cikti.Hata -> HataKutusu(c.mesaj)
            is AltAgHesaplayici.Cikti.Basarili -> {
                val s = c.sonuc
                BilgiKarti(baslik = "${s.agAdresi}/${s.onEk}", simge = Icons.Rounded.AccountTree) {
                    BilgiSatiri("Ağ adresi", s.agAdresi, esGenislik = true, kopyalanabilir = true)
                    BilgiSatiri("Yayın adresi", s.yayinAdresi, esGenislik = true, kopyalanabilir = true)
                    BilgiSatiri("Alt ağ maskesi", s.maske, esGenislik = true, kopyalanabilir = true)
                    BilgiSatiri("Joker (wildcard)", s.joker, esGenislik = true)
                    BilgiSatiri("İlk kullanılabilir", s.ilkHost, esGenislik = true)
                    BilgiSatiri("Son kullanılabilir", s.sonHost, esGenislik = true)
                    BilgiSatiri("Toplam adres", Bicim.binlik(s.toplamAdres))
                    BilgiSatiri("Kullanılabilir host", Bicim.binlik(s.kullanilabilirHost))
                    BilgiSatiri("Sınıf", s.sinif)
                    BilgiSatiri("Adres türü", s.tur)
                    if (s.onEk == 31) {
                        Text(
                            "/31: Noktadan noktaya bağlantılar için (RFC 3021); iki adres de kullanılabilir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                BilgiKarti(baslik = "İkili gösterim", simge = Icons.Rounded.Code) {
                    Text("IP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(s.ipIkili, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    Text("Maske", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(s.maskeIkili, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                }
            }
            null -> NotKutusu("İnternet gerekmez. Örnek girdiler: 10.0.0.5/8 · 172.16.4.20 + 255.255.240.0 · 192.168.1.1/30")
        }
    }
}
