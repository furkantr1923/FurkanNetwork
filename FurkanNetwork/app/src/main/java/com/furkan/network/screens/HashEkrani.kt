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
import com.furkan.network.utils.HashYardimcisi
import com.furkan.network.viewmodel.HashViewModel

@Composable
fun HashEkrani(vm: HashViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val renkler = LocalDurumRenkleri.current
    val dosyaSec = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.dosyaSecildi(uri)
    }
    EkranKapanincaDurdur { vm.dosyaTemizle() }

    fun goster(h: String) = if (d.buyukHarf) h.uppercase() else h

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = d.metin,
            onValueChange = vm::metinDegisti,
            label = { Text("Metin") },
            placeholder = { Text("Özeti alınacak metni yazın") },
            minLines = 3,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        BilgiKarti(baslik = "SHA-256 (metin, UTF-8)", simge = Icons.Rounded.Fingerprint, eylem = {
            IconButton(onClick = { panoyaKopyala(context, goster(d.metinHash)) }) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Kopyala")
            }
        }) {
            Text(goster(d.metinHash), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
            Text(
                "${d.metin.toByteArray(Charsets.UTF_8).size} bayt" + if (d.metin.isEmpty()) " (boş metnin özeti)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Büyük harf göster", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = d.buyukHarf, onCheckedChange = vm::buyukHarfDegisti)
        }

        BolumBasligi("Dosya özeti")
        IkincilButon(
            if (d.dosyaHesaplaniyor) "Hesaplanıyor…" else "Dosya seç",
            Icons.Rounded.FolderOpen,
            onClick = { dosyaSec.launch(arrayOf("*/*")) },
            etkin = !d.dosyaHesaplaniyor,
            modifier = Modifier.fillMaxWidth()
        )
        if (d.dosyaHesaplaniyor) YukleniyorSatiri("Dosya okunuyor (cihaz içinde, hiçbir yere gönderilmez)…")
        d.hata?.let { HataKutusu(it) }
        val dosyaHash = d.dosyaHash
        if (dosyaHash != null) {
            BilgiKarti(baslik = d.dosyaAdi ?: "Dosya", simge = Icons.Rounded.Description, eylem = {
                IconButton(onClick = { panoyaKopyala(context, goster(dosyaHash)) }) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Kopyala")
                }
            }) {
                Text(goster(dosyaHash), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                d.dosyaBoyutu?.let {
                    Text("Boyut: ${Bicim.bayt(it)} (${Bicim.binlik(it)} bayt)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        BolumBasligi("Karşılaştır")
        OutlinedTextField(
            value = d.beklenen,
            onValueChange = vm::beklenenDegisti,
            label = { Text("Beklenen SHA-256 değeri") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        if (d.beklenen.isNotBlank()) {
            val metinEslesti = HashYardimcisi.esitMi(d.beklenen, d.metinHash)
            val dosyaEslesti = dosyaHash != null && HashYardimcisi.esitMi(d.beklenen, dosyaHash)
            when {
                dosyaEslesti -> NotKutusu("Dosya özeti beklenen değerle EŞLEŞİYOR.", simge = Icons.Rounded.CheckCircle, renk = renkler.basari)
                metinEslesti -> NotKutusu("Metin özeti beklenen değerle EŞLEŞİYOR.", simge = Icons.Rounded.CheckCircle, renk = renkler.basari)
                else -> NotKutusu("Eşleşme yok. Değeri ve seçilen metni/dosyayı kontrol edin.", simge = Icons.Rounded.Warning, renk = renkler.hata)
            }
        }
    }
}
