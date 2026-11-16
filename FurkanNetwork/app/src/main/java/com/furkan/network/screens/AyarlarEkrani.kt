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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import com.furkan.network.BuildConfig
import com.furkan.network.ui.navigation.Rota
import com.furkan.network.viewmodel.AyarlarViewModel

@Composable
fun AyarlarEkrani(git: (Rota) -> Unit, vm: AyarlarViewModel = viewModel()) {
    val a by vm.ayarlar.collectAsStateWithLifecycle()
    val kayitSayisi by vm.kayitSayisi.collectAsStateWithLifecycle()
    val mesaj by vm.mesaj.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var temizleSor by remember { mutableStateOf(false) }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mesajGosterildi()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BilgiKarti(baslik = "Görünüm", simge = Icons.Rounded.Brightness4) {
            SecenekSatiri("Koyu tema", a.koyuTema, vm::koyuTema)
        }

        BilgiKarti(baslik = "Ağ", simge = Icons.Rounded.Public) {
            SecenekSatiri("Genel IP'yi otomatik sorgula", a.genelIpOtomatik, vm::genelIpOtomatik)
            Text(
                "Açıkken panel açıldığında api.ipify.org'a istek gönderilir. Kapalıyken yalnızca siz yenilediğinizde sorgulanır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Varsayılan ping paket sayısı", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 20).forEach { n ->
                    FilterChip(selected = a.pingAdet == n, onClick = { vm.pingAdet(n) }, label = { Text("$n") })
                }
            }
        }

        BilgiKarti(baslik = "Geçmiş", simge = Icons.Rounded.History) {
            SecenekSatiri("Test sonuçlarını kaydet", a.gecmisKaydet, vm::gecmisKaydet)
            BilgiSatiri("Kayıtlı sonuç", "$kayitSayisi")
            AnaButon(
                "Geçmişi temizle", Icons.Rounded.DeleteSweep,
                onClick = { temizleSor = true },
                etkin = kayitSayisi > 0,
                tehlikeli = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BilgiKarti(baslik = "Uygulama", simge = Icons.Rounded.Info) {
            BilgiSatiri("Sürüm", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            BilgiSatiri("Paket", BuildConfig.APPLICATION_ID, esGenislik = true)
            BilgiSatiri("Geliştirici", "Furkan Baykal")
            BilgiSatiri("Android", "8.0 (API 26) ve üzeri")
        }

        AracSatiri("Gizlilik", "Hangi veriler işlenir, hangi izinler neden istenir", Icons.Rounded.Security, { git(Rota.GIZLILIK) })
        AracSatiri("Furkan Network hakkında", "Özellikler, teknolojiler, sınırlamalar", Icons.Rounded.Info, { git(Rota.HAKKINDA) })
    }

    if (temizleSor) {
        AlertDialog(
            onDismissRequest = { temizleSor = false },
            title = { Text("Geçmiş temizlensin mi?") },
            text = { Text("$kayitSayisi kayıt kalıcı olarak silinecek.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.gecmisiTemizle()
                    temizleSor = false
                }) { Text("Temizle", color = LocalDurumRenkleri.current.hata) }
            },
            dismissButton = { TextButton(onClick = { temizleSor = false }) { Text("Vazgeç") } }
        )
    }
}
