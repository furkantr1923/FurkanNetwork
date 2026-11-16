package com.furkan.network.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Türkçe sayı/tarih biçimlendirme (ondalık ayırıcı virgül). */
object Bicim {
    val TR: Locale = Locale.forLanguageTag("tr-TR")

    fun ms(d: Double?): String = when {
        d == null -> "—"
        d < 10 -> String.format(TR, "%.2f ms", d)
        else -> String.format(TR, "%.1f ms", d)
    }

    fun sayi(d: Double, basamak: Int = 1): String = String.format(TR, "%.${basamak}f", d)

    /** Türkçe yüzde gösterimi: %12,5 */
    fun yuzde(d: Double): String = "%" + String.format(TR, "%.1f", d)

    fun binlik(l: Long): String = String.format(TR, "%,d", l)

    /** Ondalık (SI) birimlerle bayt: 1 MB = 1.000.000 bayt. */
    fun bayt(b: Long): String = when {
        b >= 1_000_000_000L -> String.format(TR, "%.2f GB", b / 1e9)
        b >= 1_000_000L -> String.format(TR, "%.1f MB", b / 1e6)
        b >= 1_000L -> String.format(TR, "%.1f KB", b / 1e3)
        else -> "$b B"
    }

    fun mbps(d: Double): String = String.format(TR, "%.1f Mbps", d)

    fun tarih(zaman: Long): String = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", TR).format(Date(zaman))

    fun gun(zaman: Long): String = SimpleDateFormat("dd.MM.yyyy", TR).format(Date(zaman))
}
