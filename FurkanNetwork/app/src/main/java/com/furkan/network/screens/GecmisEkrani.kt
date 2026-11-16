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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import com.furkan.network.data.GecmisDeposu
import com.furkan.network.models.GecmisKaydi
import com.furkan.network.models.TestTuru
import com.furkan.network.viewmodel.GecmisViewModel
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun GecmisEkrani(vm: GecmisViewModel = viewModel()) {
    val kayitlar by vm.kayitlar.collectAsStateWithLifecycle()
    val tumu by vm.toplamKayit.collectAsStateWithLifecycle()
    val filtre by vm.filtre.collectAsStateWithLifecycle()
    val mesaj by vm.mesaj.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var acikKayit by remember { mutableStateOf<GecmisKaydi?>(null) }
    var temizleSor by remember { mutableStateOf(false) }

    val disaAktar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) vm.disaAktar(uri)
    }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mesajGosterildi()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = filtre == null, onClick = { vm.filtreSec(null) }, label = { Text("Tümü (${tumu.size})") })
            TestTuru.entries.forEach { t ->
                val adet = tumu.count { it.tur == t }
                if (adet > 0) {
                    FilterChip(selected = filtre == t, onClick = { vm.filtreSec(t) }, label = { Text("${t.etiket} ($adet)") })
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IkincilButon(
                "Dışa aktar (.txt)", Icons.Rounded.FileDownload,
                onClick = {
                    val ad = "furkan-network-gecmis-" + SimpleDateFormat("yyyyMMdd-HHmm", Bicim.TR).format(Date()) + ".txt"
                    disaAktar.launch(ad)
                },
                etkin = kayitlar.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
            IkincilButon(
                "Tümünü sil", Icons.Rounded.DeleteSweep,
                onClick = { temizleSor = true },
                etkin = tumu.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        if (kayitlar.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.History, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Henüz kayıt yok", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Ping, DNS, traceroute, SSL ve hız testi sonuçları burada saklanır. Kayıtlar yalnızca bu cihazda tutulur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(kayitlar, key = { it.id }) { k ->
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { acikKayit = k }
                    ) {
                        Row(Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(turSimgesi(k.tur), contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(k.baslik, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(
                                    k.ozet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                                Text(
                                    "${k.tur.etiket} · ${Bicim.tarih(k.zaman)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { vm.sil(k.id) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Kaydı sil")
                            }
                        }
                    }
                }
            }
        }
    }

    acikKayit?.let { k ->
        AlertDialog(
            onDismissRequest = { acikKayit = null },
            icon = { Icon(turSimgesi(k.tur), contentDescription = null) },
            title = { Text(k.baslik) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(Bicim.tarih(k.zaman), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(k.ozet, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(k.ayrinti, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                }
            },
            confirmButton = {
                TextButton(onClick = { metinPaylas(context, "Furkan Network — ${k.baslik}", GecmisDeposu.kayitMetni(k)) }) {
                    Text("Paylaş")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        vm.sil(k.id)
                        acikKayit = null
                    }) { Text("Sil") }
                    TextButton(onClick = { acikKayit = null }) { Text("Kapat") }
                }
            }
        )
    }

    if (temizleSor) {
        AlertDialog(
            onDismissRequest = { temizleSor = false },
            title = { Text("Tüm geçmiş silinsin mi?") },
            text = { Text("${tumu.size} kayıt kalıcı olarak silinecek. Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.temizle()
                    temizleSor = false
                }) { Text("Sil", color = LocalDurumRenkleri.current.hata) }
            },
            dismissButton = { TextButton(onClick = { temizleSor = false }) { Text("Vazgeç") } }
        )
    }
}

fun turSimgesi(t: TestTuru) = when (t) {
    TestTuru.IP -> Icons.Rounded.Public
    TestTuru.DNS -> Icons.Rounded.Dns
    TestTuru.PING -> Icons.Rounded.NetworkCheck
    TestTuru.TRACEROUTE -> Icons.Rounded.Timeline
    TestTuru.AG -> Icons.Rounded.Wifi
    TestTuru.SSL -> Icons.Rounded.Https
    TestTuru.HIZ -> Icons.Rounded.Speed
}
