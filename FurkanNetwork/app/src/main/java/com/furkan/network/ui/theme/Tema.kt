package com.furkan.network.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val KoyuSema = darkColorScheme(
    primary = Renkler.Mavi,
    onPrimary = Renkler.Beyaz,
    primaryContainer = Renkler.MaviKap,
    onPrimaryContainer = Renkler.Beyaz,
    secondary = Renkler.AcikMavi,
    onSecondary = Renkler.Lacivert,
    secondaryContainer = Color(0xFF1B3A5C),
    onSecondaryContainer = Renkler.AcikMavi,
    tertiary = Renkler.AcikMavi,
    onTertiary = Renkler.Lacivert,
    background = Renkler.Lacivert,
    onBackground = Renkler.Beyaz,
    surface = Renkler.Lacivert,
    onSurface = Renkler.Beyaz,
    surfaceVariant = Renkler.LacivertKartYuksek,
    onSurfaceVariant = Renkler.Gri,
    surfaceContainerLowest = Renkler.LacivertDerin,
    surfaceContainerLow = Renkler.LacivertAlt,
    surfaceContainer = Renkler.LacivertKart,
    surfaceContainerHigh = Renkler.LacivertKartYuksek,
    surfaceContainerHighest = Renkler.LacivertEnYuksek,
    outline = Renkler.LacivertCizgi,
    outlineVariant = Renkler.LacivertCizgiAcik,
    error = KoyuDurumRenkleri.hata,
    onError = Renkler.Lacivert
)

private val AcikSema = lightColorScheme(
    primary = Renkler.KoyuMavi,
    onPrimary = Renkler.Beyaz,
    primaryContainer = Color(0xFFD6E6FF),
    onPrimaryContainer = Color(0xFF0A2A5C),
    secondary = Color(0xFF0B7FA8),
    onSecondary = Renkler.Beyaz,
    secondaryContainer = Color(0xFFD3F1FC),
    onSecondaryContainer = Color(0xFF053B4F),
    tertiary = Color(0xFF0B7FA8),
    background = Renkler.AcikArka,
    onBackground = Renkler.AcikMetin,
    surface = Renkler.AcikArka,
    onSurface = Renkler.AcikMetin,
    surfaceVariant = Renkler.AcikKartYuksek,
    onSurfaceVariant = Renkler.AcikIkincil,
    surfaceContainerLowest = Renkler.AcikKart,
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = Renkler.AcikKart,
    surfaceContainerHigh = Renkler.AcikKartYuksek,
    surfaceContainerHighest = Color(0xFFDCE5F2),
    outline = Renkler.AcikCizgi,
    outlineVariant = Color(0xFFE0E7F1),
    error = AcikDurumRenkleri.hata,
    onError = Renkler.Beyaz
)

private val Varsayilan = Typography()

private val FnYazi = Typography(
    headlineSmall = Varsayilan.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Varsayilan.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Varsayilan.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Varsayilan.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Varsayilan.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

private val FnSekiller = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FurkanNetworkTema(koyuTema: Boolean = true, icerik: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalDurumRenkleri provides if (koyuTema) KoyuDurumRenkleri else AcikDurumRenkleri
    ) {
        MaterialTheme(
            colorScheme = if (koyuTema) KoyuSema else AcikSema,
            typography = FnYazi,
            shapes = FnSekiller,
            content = icerik
        )
    }
}
