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
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun GizlilikEkrani() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(EkranDolgusu),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NotKutusu(
            "Furkan Network reklam, analiz (analytics) veya takip aracı içermez. Hesap gerekmez, sunucumuz yoktur.",
            simge = Icons.Rounded.Security,
            renk = LocalDurumRenkleri.current.basari
        )
        Madde(Icons.Rounded.Storage, "Cihazda saklananlar",
            "Test geçmişi ve ayarlar yalnızca uygulamanın özel klasöründe tutulur. Yedekleme kapalıdır (allowBackup=false). " +
                "Uygulamayı kaldırdığınızda veya geçmişi temizlediğinizde silinir.")
        Madde(Icons.Rounded.Public, "Dışarıya giden istekler (yalnızca siz test başlatınca)",
            "• Genel IP: api.ipify.org / api6.ipify.org (ayarlardan otomatik sorgu kapatılabilir)\n" +
                "• DNS: cihazınızın kullandığı DNS sunucusu\n" +
                "• Ping / Traceroute: girdiğiniz hedef adres\n" +
                "• SSL: girdiğiniz sunucu (yalnızca TLS el sıkışması)\n" +
                "• Hız testi: speed.cloudflare.com (isteğe bağlı)")
        Madde(Icons.Rounded.VpnKey, "İzinler",
            "• İnternet: ağ testleri için\n" +
                "• Ağ durumu: bağlantı türü, IP, DNS sunucuları\n" +
                "• Wi-Fi durumu: sinyal gücü, hız, frekans\n" +
                "• Konum (isteğe bağlı): yalnızca Wi-Fi ağ adını (SSID) göstermek için; Android bu bilgiyi konum izni olmadan gizler. " +
                "Uygulama konumunuzu okumaz, saklamaz, göndermez.")
        Madde(Icons.Rounded.Lock, "Şifreler ve özetler",
            "Şifre üretici ve SHA-256 tamamen cihaz içinde çalışır. Üretilen şifreler geçmişe kaydedilmez. " +
                "Seçtiğiniz dosyalar yalnızca okunur, hiçbir yere gönderilmez.")
        Madde(Icons.Rounded.FileDownload, "Dışa aktarma",
            "Geçmişi dışa aktardığınızda metin dosyası yalnızca sizin seçtiğiniz konuma yazılır.")
    }
}

@Composable
private fun Madde(simge: ImageVector, baslik: String, metin: String) {
    BilgiKarti(baslik = baslik, simge = simge) {
        Text(metin, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
