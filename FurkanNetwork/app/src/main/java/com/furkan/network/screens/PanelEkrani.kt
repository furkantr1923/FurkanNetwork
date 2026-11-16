@file:OptIn(ExperimentalMaterial3Api::class)

package com.furkan.network.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.furkan.network.components.*
import com.furkan.network.models.AgDurumu
import com.furkan.network.models.GenelIpDurumu
import com.furkan.network.ui.navigation.Rota
import com.furkan.network.ui.theme.LocalDurumRenkleri
import com.furkan.network.viewmodel.PanelViewModel

@Composable
fun PanelEkrani(git: (Rota) -> Unit, vm: PanelViewModel = viewModel()) {
    val ag by vm.agDurumu.collectAsStateWithLifecycle()
    val genelIp by vm.genelIp.collectAsStateWithLifecycle()
    val ayarlar by vm.ayarlar.collectAsStateWithLifecycle()

    LaunchedEffect(ag.kimlik, ag.internetDogrulandi, ag.bagli) { vm.agDegisti(ag) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FurkanLogo(boyut = 52.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Ağ analiz ve tanı araç seti", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Gerçek ölçümler · reklamsız · takip yok",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        BaglantiDurumKarti(ag)

        BilgiKarti(baslik = "Adresler", simge = Icons.Rounded.Public) {
            BilgiSatiri("Yerel IPv4", ag.yerelIpv4.firstOrNull() ?: "—", esGenislik = true, kopyalanabilir = true)
            GenelIpSatiri(genelIp, otomatik = ayarlar.genelIpOtomatik, bagli = ag.bagli, yenile = vm::genelIpYenile)
            BilgiSatiri(
                "DNS sunucusu",
                ag.dnsSunuculari.firstOrNull()?.let { ilk ->
                    if (ag.dnsSunuculari.size > 1) "$ilk (+${ag.dnsSunuculari.size - 1})" else ilk
                } ?: "—",
                esGenislik = true
            )
            ag.ozelDns?.let { BilgiSatiri("Özel DNS", it) }
        }

        BolumBasligi("Hızlı erişim")
        val araclar = listOf(
            Triple("IP Bilgisi", Icons.Rounded.Public, Rota.IP),
            Triple("DNS", Icons.Rounded.Dns, Rota.DNS),
            Triple("Ping", Icons.Rounded.NetworkCheck, Rota.PING),
            Triple("Traceroute", Icons.Rounded.Timeline, Rota.TRACEROUTE),
            Triple("Ağ Bilgisi", Icons.Rounded.Wifi, Rota.AG),
            Triple("Hız Testi", Icons.Rounded.Speed, Rota.HIZ)
        )
        araclar.chunked(3).forEach { satir ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                satir.forEach { (ad, simge, rota) ->
                    HizliErisim(ad, simge, onClick = { git(rota) }, modifier = Modifier.weight(1f))
                }
            }
        }

        NotKutusu(
            "Alt ağ hesaplayıcı, SHA-256 ve şifre üretici internet olmadan da çalışır. Araçlar sekmesinden ulaşabilirsiniz.",
            simge = Icons.Rounded.CloudOff
        )
    }
}

@Composable
private fun BaglantiDurumKarti(ag: AgDurumu) {
    val renkler = LocalDurumRenkleri.current
    val (metin, renk, simge) = when {
        !ag.bagli -> Triple("Bağlantı yok", renkler.hata, Icons.Rounded.CloudOff)
        ag.internetDogrulandi -> Triple("İnternet erişimi var", renkler.basari, Icons.Rounded.CheckCircle)
        else -> Triple("Bağlı, internet doğrulanmadı", renkler.uyari, Icons.Rounded.Warning)
    }
    BilgiKarti {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(simge, contentDescription = null, tint = renk, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(metin, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Bağlantı türü: ${ag.tur.etiket}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (ag.bagli) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DurumRozeti(ag.tur.etiket, MaterialTheme.colorScheme.secondary)
                if (ag.vpnAktif) DurumRozeti("VPN", renkler.uyari)
                if (ag.olcumlu) DurumRozeti("Ölçümlü", renkler.uyari)
            }
        }
        if (ag.bagli && !ag.internetDogrulandi) {
            Text(
                "Android bu ağda internet erişimini doğrulayamadı (giriş sayfası, kısıtlı ağ veya bağlantı sorunu olabilir).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GenelIpSatiri(durum: GenelIpDurumu, otomatik: Boolean, bagli: Boolean, yenile: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            when (durum) {
                is GenelIpDurumu.Basarili -> {
                    Column {
                        BilgiSatiri("Genel IPv4", durum.ipv4 ?: "Yok", esGenislik = true, kopyalanabilir = durum.ipv4 != null)
                        if (durum.ipv6 != null) {
                            BilgiSatiri("Genel IPv6", durum.ipv6, esGenislik = true, kopyalanabilir = true)
                        }
                    }
                }
                GenelIpDurumu.Yukleniyor -> YukleniyorSatiri("Genel IP sorgulanıyor…")
                is GenelIpDurumu.Hata -> BilgiSatiri("Genel IP", "Alınamadı", degerRengi = LocalDurumRenkleri.current.hata)
                GenelIpDurumu.Bos -> BilgiSatiri(
                    "Genel IP",
                    when {
                        !bagli -> "Bağlantı yok"
                        !otomatik -> "Otomatik sorgu kapalı"
                        else -> "Bekleniyor"
                    }
                )
            }
        }
        IconButton(onClick = yenile, enabled = bagli && durum !is GenelIpDurumu.Yukleniyor) {
            Icon(Icons.Rounded.Refresh, contentDescription = "Genel IP'yi yenile")
        }
    }
    if (durum is GenelIpDurumu.Hata) {
        Text(durum.mesaj, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
