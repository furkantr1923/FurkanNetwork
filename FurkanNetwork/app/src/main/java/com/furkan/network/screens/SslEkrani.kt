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
import com.furkan.network.models.SertifikaBilgisi
import com.furkan.network.viewmodel.SslViewModel

@Composable
fun SslEkrani(vm: SslViewModel = viewModel()) {
    val d by vm.durum.collectAsStateWithLifecycle()
    val renkler = LocalDurumRenkleri.current
    EkranKapanincaDurdur { vm.iptal() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HostAlani(
            deger = d.girdi,
            degisti = vm::girdiDegisti,
            etiket = "Alan adı (isteğe bağlı :port)",
            ornek = "google.com",
            calistir = vm::incele,
            etkin = !d.yukleniyor,
            hata = d.hata
        )
        AnaButon(
            "Sertifikayı incele", Icons.Rounded.Https, onClick = vm::incele,
            etkin = !d.yukleniyor && d.girdi.isNotBlank(), modifier = Modifier.fillMaxWidth()
        )
        if (d.yukleniyor) YukleniyorSatiri("TLS el sıkışması yapılıyor…")

        val s = d.sonuc
        if (s != null) {
            val yaprak = s.zincir.first()
            val simdi = System.currentTimeMillis()
            val kalanGun = (yaprak.bitis - simdi) / 86_400_000L
            val gecerli = simdi in yaprak.baslangic..yaprak.bitis

            BilgiKarti(baslik = "${s.host}:${s.port}", simge = Icons.Rounded.Lock) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DurumRozeti(if (gecerli) "Geçerli" else "Süresi dışında", if (gecerli) renkler.basari else renkler.hata)
                    DurumRozeti(if (s.adDogrulandi) "Ad uyumlu" else "Ad uyumsuz", if (s.adDogrulandi) renkler.basari else renkler.hata)
                }
                BilgiSatiri("Protokol", s.protokol)
                BilgiSatiri("Şifre takımı", s.sifreTakimi, esGenislik = true)
                BilgiSatiri("Bağlantı süresi", "${s.sureMs} ms")
                BilgiSatiri(
                    "Kalan süre",
                    if (kalanGun >= 0) "$kalanGun gün" else "${-kalanGun} gün önce doldu",
                    degerRengi = when {
                        kalanGun < 0 -> renkler.hata
                        kalanGun < 15 -> renkler.uyari
                        else -> renkler.basari
                    }
                )
            }
            SertifikaKarti("Sunucu sertifikası", yaprak)
            if (s.zincir.size > 1) {
                BilgiKarti(baslik = "Sertifika zinciri (${s.zincir.size})", simge = Icons.Rounded.AccountTree) {
                    s.zincir.forEachIndexed { i, c ->
                        Text(
                            "${"  ".repeat(i)}${if (i == 0) "●" else "└"} ${c.konuKisa}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    Text(
                        "Zincir Android'in güvenilir kök sertifika deposuyla doğrulandı.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SertifikaKarti(baslik: String, c: SertifikaBilgisi) {
    BilgiKarti(baslik = baslik, simge = Icons.Rounded.Security) {
        BilgiSatiri("Konu (CN)", c.konuKisa)
        BilgiSatiri("Yayıncı", c.yayinciKisa)
        BilgiSatiri("Geçerlilik başı", Bicim.gun(c.baslangic))
        BilgiSatiri("Geçerlilik sonu", Bicim.gun(c.bitis))
        BilgiSatiri("Anahtar", c.anahtar)
        BilgiSatiri("İmza", c.imzaAlgoritmasi)
        BilgiSatiri("Seri no", c.seriNo, esGenislik = true)
        if (c.alternatifAdlar.isNotEmpty()) {
            Text("Alternatif adlar (SAN)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                c.alternatifAdlar.take(20).joinToString(", ") +
                    if (c.alternatifAdlar.size > 20) " … (+${c.alternatifAdlar.size - 20})" else "",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
            )
        }
        Text("SHA-256 parmak izi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(c.sha256ParmakIzi, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
    }
}
