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
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.furkan.network.utils.SinyalYardimcisi
import com.furkan.network.viewmodel.AgBilgiViewModel

@Composable
fun AgBilgiEkrani(vm: AgBilgiViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val renkler = LocalDurumRenkleri.current
    val context = LocalContext.current
    val izinIste = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.yenile()
    }
    val ag = d.ag
    val kb = d.kablosuz

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BilgiKarti(baslik = "Bağlantı", simge = Icons.Rounded.NetworkCheck) {
            BilgiSatiri("Bağlantı türü", ag.tur.etiket)
            BilgiSatiri(
                "İnternet erişimi",
                when {
                    !ag.bagli -> "Yok"
                    ag.internetDogrulandi -> "Var (doğrulandı)"
                    else -> "Doğrulanmadı"
                },
                degerRengi = when {
                    !ag.bagli -> renkler.hata
                    ag.internetDogrulandi -> renkler.basari
                    else -> renkler.uyari
                }
            )
            BilgiSatiri("VPN", if (ag.vpnAktif) "Etkin" else "Kapalı")
            BilgiSatiri("Ölçümlü bağlantı", if (!ag.bagli) "—" else if (ag.olcumlu) "Evet (kotaya tabi olabilir)" else "Hayır")
            ag.tahminiIndirmeKbps?.let { BilgiSatiri("Tahmini indirme", "${Bicim.sayi(it / 1000.0)} Mbps (Android tahmini)") }
            ag.tahminiYuklemeKbps?.let { BilgiSatiri("Tahmini yükleme", "${Bicim.sayi(it / 1000.0)} Mbps (Android tahmini)") }
        }

        BilgiKarti(baslik = "Wi-Fi", simge = Icons.Rounded.Wifi) {
            if (kb == null) {
                YukleniyorSatiri("Okunuyor…")
            } else {
                BilgiSatiri(
                    "Durum",
                    when {
                        kb.wifiBagli -> "Bağlı"
                        kb.wifiAcik == true -> "Açık, bağlı değil"
                        kb.wifiAcik == false -> "Kapalı"
                        else -> "Bilinmiyor"
                    }
                )
                if (kb.wifiBagli) {
                    BilgiSatiri("Ağ adı (SSID)", kb.ssid ?: "Gizli (izin gerekli)")
                    val seviye = SinyalYardimcisi.seviye(kb.rssi)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Sinyal gücü",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        SinyalCubuklari(seviye)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (kb.rssi != null) "${kb.rssi} dBm · ${SinyalYardimcisi.seviyeEtiketi(seviye)}" else "Okunamadı",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    BilgiSatiri("Bağlantı hızı", kb.baglantiHiziMbps?.let { "$it Mbps" } ?: "—")
                    BilgiSatiri(
                        "Frekans",
                        kb.frekansMhz?.let { f ->
                            listOfNotNull("$f MHz", SinyalYardimcisi.bant(f), SinyalYardimcisi.kanal(f)?.let { "kanal $it" })
                                .joinToString(" · ")
                        } ?: "—"
                    )
                    if (kb.ssid == null && !d.konumIzni) {
                        Text(
                            "Android, Wi-Fi ağ adını yalnızca konum izni verildiğinde gösterir. " +
                                "Furkan Network konumunuzu okumaz ve kaydetmez; izin yalnızca ağ adı için kullanılır.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IkincilButon("Ağ adını göster (izin iste)", Icons.Rounded.Visibility, onClick = {
                            izinIste.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        })
                    } else if (kb.ssid == null && !d.konumAcik) {
                        Text(
                            "Konum izni var ancak cihazın konum servisi kapalı; Android bu durumda ağ adını gizler.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        BilgiKarti(baslik = "Mobil veri", simge = Icons.Rounded.SignalCellularAlt) {
            if (kb == null) {
                YukleniyorSatiri("Okunuyor…")
            } else if (!kb.telefonDestegi) {
                Text("Bu cihazda mobil ağ donanımı yok.", style = MaterialTheme.typography.bodyMedium)
            } else {
                BilgiSatiri(
                    "Durum",
                    when {
                        kb.mobilBagli -> "Etkin bağlantı"
                        kb.mobilVeriAcik == true -> "Açık (Wi-Fi kullanılıyor)"
                        kb.mobilVeriAcik == false -> "Kapalı"
                        else -> "Bilinmiyor"
                    }
                )
                BilgiSatiri("SIM kart", when (kb.simVar) { true -> "Takılı"; false -> "Yok"; null -> "Bilinmiyor" })
                kb.operator?.let { BilgiSatiri("Operatör", it) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Sinyal seviyesi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    SinyalCubuklari(kb.mobilSinyalSeviyesi)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        kb.mobilSinyalSeviyesi?.let { "$it / 4" } ?: "Okunamadı",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        kb?.notlar?.takeIf { it.isNotEmpty() }?.let { notlar ->
            NotKutusu(notlar.joinToString("\n"))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnaButon("Yenile", Icons.Rounded.Refresh, onClick = vm::yenile, modifier = Modifier.weight(1f))
            IkincilButon("Geçmişe kaydet", Icons.Rounded.Save, onClick = {
                vm.gecmiseKaydet()
                android.widget.Toast.makeText(context, "Geçmişe kaydedildi", android.widget.Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.weight(1f))
        }
        Text(
            "Bilgiler ekran açıkken 3 saniyede bir yenilenir. Son güncelleme: " +
                if (d.guncelleme > 0) Bicim.tarih(d.guncelleme) else "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
