package com.furkan.network.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Furkan Network renk paleti: koyu lacivert, mavi ve beyaz. */
object Renkler {
    val Lacivert = Color(0xFF0A1628)
    val LacivertDerin = Color(0xFF07101E)
    val LacivertAlt = Color(0xFF0F1D33)
    val LacivertKart = Color(0xFF12233D)
    val LacivertKartYuksek = Color(0xFF1A2F4F)
    val LacivertEnYuksek = Color(0xFF223A5E)
    val LacivertCizgi = Color(0xFF2A4166)
    val LacivertCizgiAcik = Color(0xFF1F3354)
    val Mavi = Color(0xFF2F80ED)
    val MaviKap = Color(0xFF173A6B)
    val AcikMavi = Color(0xFF56CCF2)
    val Beyaz = Color(0xFFFFFFFF)
    val Gri = Color(0xFFA9B6C8)

    val AcikArka = Color(0xFFF3F6FB)
    val AcikKart = Color(0xFFFFFFFF)
    val AcikKartYuksek = Color(0xFFE7EEF8)
    val AcikMetin = Color(0xFF0B1A2E)
    val AcikIkincil = Color(0xFF4A5B73)
    val KoyuMavi = Color(0xFF1C64D1)
    val AcikCizgi = Color(0xFFC9D4E4)
}

/** Başarı / uyarı / hata renkleri (temaya göre). */
@Immutable
data class DurumRenkleri(
    val basari: Color,
    val uyari: Color,
    val hata: Color
)

val KoyuDurumRenkleri = DurumRenkleri(
    basari = Color(0xFF3DDC97),
    uyari = Color(0xFFF2C94C),
    hata = Color(0xFFFF6B6B)
)

val AcikDurumRenkleri = DurumRenkleri(
    basari = Color(0xFF16875A),
    uyari = Color(0xFF9A6B00),
    hata = Color(0xFFC62828)
)

val LocalDurumRenkleri = staticCompositionLocalOf { KoyuDurumRenkleri }
