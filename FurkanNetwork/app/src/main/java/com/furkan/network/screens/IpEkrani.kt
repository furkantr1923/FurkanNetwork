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
import com.furkan.network.viewmodel.PanelViewModel

@Composable
fun IpEkrani(vm: PanelViewModel = viewModel()) {
    val ag by vm.agDurumu.collectAsStateWithLifecycle()
    val genelIp by vm.genelIp.collectAsStateWithLifecycle()
    val ayarlar by vm.ayarlar.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(ag.kimlik, ag.internetDogrulandi, ag.bagli) { vm.agDegisti(ag) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!ag.bagli) {
            HataKutusu("Cihaz şu anda hiçbir ağa bağlı değil. Yerel ve genel IP adresi yok.")
        }

        BilgiKarti(baslik = "Yerel adresler", simge = Icons.Rounded.Router) {
            BilgiSatiri("Bağlantı türü", ag.tur.etiket + if (ag.vpnAktif) " + VPN" else "")
            if (ag.yerelIpv4.isEmpty()) {
                BilgiSatiri("IPv4", "—")
            } else {
                ag.yerelIpv4.forEachIndexed { i, ip ->
                    BilgiSatiri(if (i == 0) "IPv4" else "IPv4 (${i + 1})", ip, esGenislik = true, kopyalanabilir = true)
                }
            }
            if (ag.yerelIpv6.isEmpty()) {
                BilgiSatiri("IPv6", "Yok")
            } else {
                ag.yerelIpv6.forEach { ip ->
                    val yerel = com.furkan.network.utils.IpAdresi.ipv6BaglantiYerelMi(ip)
                    BilgiSatiri(if (yerel) "IPv6 (bağlantı-yerel)" else "IPv6", ip, esGenislik = true, kopyalanabilir = true)
                }
            }
            BilgiSatiri("Ağ geçidi", ag.agGecidi ?: "Erişilemiyor", esGenislik = ag.agGecidi != null, kopyalanabilir = ag.agGecidi != null)
            ag.arayuzAdi?.let { BilgiSatiri("Arayüz", it, esGenislik = true) }
        }

        BilgiKarti(baslik = "Genel (internet) adres", simge = Icons.Rounded.Public) {
            GenelIpSatiri(genelIp, otomatik = ayarlar.genelIpOtomatik, bagli = ag.bagli, yenile = vm::genelIpYenile)
            Text(
                "Genel IP, api.ipify.org servisine yapılan HTTPS isteğiyle öğrenilir. Ayarlardan otomatik sorguyu kapatabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BilgiKarti(baslik = "DNS", simge = Icons.Rounded.Dns) {
            if (ag.dnsSunuculari.isEmpty()) {
                BilgiSatiri("DNS sunucuları", "Erişilemiyor")
            } else {
                ag.dnsSunuculari.forEachIndexed { i, d ->
                    BilgiSatiri("DNS ${i + 1}", d, esGenislik = true, kopyalanabilir = true)
                }
            }
            ag.ozelDns?.let { BilgiSatiri("Özel DNS (DoT)", it) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnaButon("Yenile", Icons.Rounded.Refresh, onClick = vm::genelIpYenile, etkin = ag.bagli, modifier = Modifier.weight(1f))
            IkincilButon("Geçmişe kaydet", Icons.Rounded.Save, onClick = {
                vm.gecmiseKaydet()
                android.widget.Toast.makeText(context, "Geçmişe kaydedildi", android.widget.Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.weight(1f))
        }
    }
}
