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
import com.furkan.network.BuildConfig

@Composable
fun HakkindaEkrani() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FurkanLogo(boyut = 72.dp)
        Text("Furkan Network", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Sürüm ${BuildConfig.VERSION_NAME} · Geliştirici: Furkan Baykal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BilgiKarti(baslik = "Nedir?", simge = Icons.Rounded.Info) {
            Text(
                "Hafif, reklamsız bir Android ağ analiz ve tanı araç setidir. Bağlantı durumunu, IP ve DNS bilgilerini gösterir; " +
                    "ping, traceroute, DNS sorgusu, SSL sertifika incelemesi ve isteğe bağlı indirme hızı testi yapar. " +
                    "Alt ağ hesaplayıcı, SHA-256 ve şifre üretici internet olmadan çalışır.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        BilgiKarti(baslik = "Teknolojiler", simge = Icons.Rounded.Code) {
            BilgiSatiri("Dil", "Kotlin")
            BilgiSatiri("Arayüz", "Jetpack Compose · Material 3")
            BilgiSatiri("Mimari", "MVVM · Coroutines · StateFlow")
            BilgiSatiri("Depolama", "JSON dosyası + SharedPreferences")
            BilgiSatiri("Ağ", "Android ağ API'leri (ConnectivityManager, DnsResolver, SSLSocket)")
        }
        BilgiKarti(baslik = "Dürüst sınırlamalar", simge = Icons.Rounded.Warning) {
            Text(
                "• Bazı cihazlar uygulamaların ICMP ping göndermesine izin vermez; o durumda TCP 443 bağlantı süresi ölçülür ve bu açıkça yazılır.\n" +
                    "• Traceroute yalnızca IPv4'te çalışır; ağ ICMP yanıtlarını engelliyorsa rota gösterilemez (sahte atlama gösterilmez).\n" +
                    "• Yükleme testi, port testi ve ağ taraması bu sürümde yoktur.\n" +
                    "• Mobil ağ türü (4G/5G) ek telefon izni gerektirdiği için gösterilmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "Bağımsız bir kişisel projedir. Uygulama yalnızca temel ve yasal bağlantı kontrolleri yapar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
