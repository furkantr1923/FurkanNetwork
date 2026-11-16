package com.furkan.network.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Uygulamadaki tüm ekranlar. [ustSeviye] ekranlar alt gezinti çubuğunda yer alır. */
enum class Rota(val yol: String, val baslik: String, val ustSeviye: Boolean = false) {
    PANEL("panel", "Furkan Network", true),
    ARACLAR("araclar", "Araçlar", true),
    GECMIS("gecmis", "Geçmiş", true),
    AYARLAR("ayarlar", "Ayarlar", true),
    IP("ip", "IP Bilgisi"),
    DNS("dns", "DNS Sorgusu"),
    PING("ping", "Ping Testi"),
    TRACEROUTE("traceroute", "Traceroute"),
    AG("ag", "Ağ Bilgisi"),
    ALT_AG("altag", "IPv4 Alt Ağ Hesaplayıcı"),
    HASH("hash", "SHA-256 Hesaplayıcı"),
    SIFRE("sifre", "Şifre Üretici"),
    SSL("ssl", "SSL Sertifika Bilgisi"),
    HIZ("hiz", "İndirme Hızı Testi"),
    GIZLILIK("gizlilik", "Gizlilik"),
    HAKKINDA("hakkinda", "Hakkında");

    companion object {
        fun yoldan(yol: String?): Rota = entries.firstOrNull { it.yol == yol } ?: PANEL
    }
}

data class AltSekme(val rota: Rota, val etiket: String, val simge: ImageVector)

val AltSekmeler = listOf(
    AltSekme(Rota.PANEL, "Panel", Icons.Rounded.Dashboard),
    AltSekme(Rota.ARACLAR, "Araçlar", Icons.Rounded.Build),
    AltSekme(Rota.GECMIS, "Geçmiş", Icons.Rounded.History),
    AltSekme(Rota.AYARLAR, "Ayarlar", Icons.Rounded.Settings)
)
