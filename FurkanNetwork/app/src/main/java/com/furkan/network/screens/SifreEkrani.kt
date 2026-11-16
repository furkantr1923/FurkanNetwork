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
import com.furkan.network.utils.SifreUretici
import com.furkan.network.viewmodel.SifreViewModel
import kotlin.math.roundToInt

@Composable
fun SifreEkrani(vm: SifreViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val renkler = LocalDurumRenkleri.current
    val s = d.secenekler

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BilgiKarti(baslik = "Üretilen şifre", simge = Icons.Rounded.VpnKey) {
            if (d.hata != null) {
                HataKutusu(d.hata ?: "")
            } else {
                Text(
                    d.sifre,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace)
                )
                val guc = SifreUretici.gucEtiketi(d.entropi)
                val gucRengi = when (guc) {
                    "Zayıf" -> renkler.hata
                    "Orta" -> renkler.uyari
                    else -> renkler.basari
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DurumRozeti(guc, gucRengi)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "≈ ${d.entropi.roundToInt()} bit entropi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnaButon("Yeni üret", Icons.Rounded.Refresh, onClick = vm::uret, modifier = Modifier.weight(1f))
                IkincilButon(
                    "Kopyala", Icons.Rounded.ContentCopy,
                    onClick = { panoyaKopyala(context, d.sifre, hassas = true) },
                    etkin = d.sifre.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        BilgiKarti(baslik = "Seçenekler", simge = Icons.Rounded.Tune) {
            Text("Uzunluk: ${s.uzunluk}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = s.uzunluk.toFloat(),
                onValueChange = { vm.secenekDegisti(s.copy(uzunluk = it.roundToInt())) },
                valueRange = SifreUretici.EN_AZ.toFloat()..SifreUretici.EN_COK.toFloat()
            )
            SecenekSatiri("Küçük harf (a–z)", s.kucuk) { vm.secenekDegisti(s.copy(kucuk = it)) }
            SecenekSatiri("Büyük harf (A–Z)", s.buyuk) { vm.secenekDegisti(s.copy(buyuk = it)) }
            SecenekSatiri("Rakam (0–9)", s.rakam) { vm.secenekDegisti(s.copy(rakam = it)) }
            SecenekSatiri("Sembol (!@#…)", s.sembol) { vm.secenekDegisti(s.copy(sembol = it)) }
            SecenekSatiri("Benzer karakterleri çıkar (O 0 o I l 1)", s.benzerleriHaricTut) {
                vm.secenekDegisti(s.copy(benzerleriHaricTut = it))
            }
        }
        NotKutusu(
            "Şifreler cihazda SecureRandom ile üretilir; hiçbir yere gönderilmez ve geçmişe kaydedilmez. " +
                "Kopyalarken Android 13+ panoda hassas içerik olarak işaretlenir.",
            simge = Icons.Rounded.Lock
        )
    }
}

@Composable
fun SecenekSatiri(metin: String, secili: Boolean, degisti: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(metin, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = secili, onCheckedChange = degisti)
    }
}
