package com.furkan.network.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.furkan.network.ui.theme.LocalDurumRenkleri
import com.furkan.network.utils.Bicim

/**
 * Basit çizgi grafik (harici kütüphane yok). null değerler kayıp olarak
 * alt kenarda kırmızı nokta ile gösterilir ve çizgiyi böler.
 */
@Composable
fun CizgiGrafik(
    degerler: List<Double?>,
    modifier: Modifier = Modifier,
    birim: String = "ms",
    yukseklik: Dp = 140.dp
) {
    val cizgi = MaterialTheme.colorScheme.secondary
    val izgara = MaterialTheme.colorScheme.outlineVariant
    val kayip = LocalDurumRenkleri.current.hata
    val gecerli = degerler.filterNotNull()
    val enBuyuk = (gecerli.maxOrNull() ?: 1.0).coerceAtLeast(1.0) * 1.1
    val noktaGoster = degerler.size <= 40

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Üst sınır ≈ ${Bicim.sayi(enBuyuk, 0)} $birim",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (degerler.any { it == null }) {
                Canvas(Modifier.size(8.dp)) { drawCircle(kayip) }
                Spacer(Modifier.width(4.dp))
                Text("kayıp", style = MaterialTheme.typography.labelSmall, color = kayip)
            }
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(yukseklik)
        ) {
            val g = size.width
            val y = size.height
            val pay = 6.dp.toPx()
            for (i in 0..3) {
                val cy = y * i / 3f
                drawLine(izgara, Offset(0f, cy), Offset(g, cy), strokeWidth = 1f)
            }
            if (degerler.isEmpty()) return@Canvas
            val adim = if (degerler.size > 1) g / (degerler.size - 1) else 0f
            val tekX = degerler.size == 1
            val yol = Path()
            var acik = false
            degerler.forEachIndexed { i, d ->
                val x = if (tekX) g / 2f else i * adim
                if (d == null) {
                    drawCircle(kayip, radius = 3.dp.toPx(), center = Offset(x, y - 3.dp.toPx()))
                    acik = false
                } else {
                    val cy = y - pay - ((d / enBuyuk).toFloat() * (y - 2 * pay))
                    if (!acik) {
                        yol.moveTo(x, cy)
                        acik = true
                    } else {
                        yol.lineTo(x, cy)
                    }
                    if (noktaGoster) drawCircle(cizgi, radius = 2.5.dp.toPx(), center = Offset(x, cy))
                }
            }
            drawPath(
                yol, cizgi,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/** Minimal ağ bağlantısı logosu (uygulama simgesiyle aynı çizim). */
@Composable
fun FurkanLogo(modifier: Modifier = Modifier, boyut: Dp = 48.dp) {
    val zemin = MaterialTheme.colorScheme.primary
    val dugum = Color(0xFF56CCF2)
    val beyaz = Color.White
    Canvas(modifier.size(boyut)) {
        val s = size.minDimension
        drawRoundRect(zemin, cornerRadius = CornerRadius(s * 0.28f, s * 0.28f))
        val merkez = Offset(s * 0.5f, s * 0.54f)
        val ust = Offset(s * 0.5f, s * 0.24f)
        val sol = Offset(s * 0.25f, s * 0.73f)
        val sag = Offset(s * 0.75f, s * 0.73f)
        drawLine(beyaz.copy(alpha = 0.5f), sol, sag, strokeWidth = s * 0.035f, cap = StrokeCap.Round)
        listOf(ust, sol, sag).forEach {
            drawLine(beyaz, merkez, it, strokeWidth = s * 0.055f, cap = StrokeCap.Round)
        }
        listOf(ust, sol, sag).forEach { drawCircle(dugum, radius = s * 0.075f, center = it) }
        drawCircle(beyaz, radius = s * 0.1f, center = merkez)
    }
}
