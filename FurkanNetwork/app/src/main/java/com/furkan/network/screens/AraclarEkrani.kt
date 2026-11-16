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
import com.furkan.network.ui.navigation.Rota

@Composable
fun AraclarEkrani(git: (Rota) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BolumBasligi("Ağ araçları")
        AracSatiri("IP Bilgisi", "Yerel IPv4/IPv6, genel IP, ağ geçidi ve DNS sunucuları", Icons.Rounded.Public, { git(Rota.IP) })
        AracSatiri("DNS Sorgusu", "A ve AAAA kayıtları, sorgu süresi", Icons.Rounded.Dns, { git(Rota.DNS) })
        AracSatiri("Ping Testi", "Gecikme, en az/ortalama/en çok, paket kaybı, grafik", Icons.Rounded.NetworkCheck, { git(Rota.PING) })
        AracSatiri("Traceroute", "Hedefe giden yol üzerindeki atlamalar", Icons.Rounded.Timeline, { git(Rota.TRACEROUTE) })
        AracSatiri("Ağ Bilgisi", "Wi-Fi / mobil veri durumu ve sinyal seviyesi", Icons.Rounded.Wifi, { git(Rota.AG) })
        AracSatiri("SSL Sertifika Bilgisi", "HTTPS sertifikası, geçerlilik, yayıncı, TLS sürümü", Icons.Rounded.Https, { git(Rota.SSL) })
        AracSatiri("İndirme Hızı Testi", "İsteğe bağlı; Cloudflare test sunucusu ile ölçüm", Icons.Rounded.Speed, { git(Rota.HIZ) })

        BolumBasligi("Çevrimdışı araçlar")
        AracSatiri("Alt Ağ Hesaplayıcı", "IPv4 ağ/yayın adresi, maske, host aralığı", Icons.Rounded.Calculate, { git(Rota.ALT_AG) }, rozet = "Çevrimdışı")
        AracSatiri("SHA-256", "Metin veya dosya özeti, karşılaştırma", Icons.Rounded.Fingerprint, { git(Rota.HASH) }, rozet = "Çevrimdışı")
        AracSatiri("Şifre Üretici", "Güvenli rastgele şifre (SecureRandom)", Icons.Rounded.VpnKey, { git(Rota.SIFRE) }, rozet = "Çevrimdışı")
    }
}
