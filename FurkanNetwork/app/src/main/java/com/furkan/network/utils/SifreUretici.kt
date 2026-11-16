package com.furkan.network.utils

import java.security.SecureRandom
import kotlin.math.log2

/** Kriptografik olarak güvenli rastgele şifre üretici (SecureRandom). */
object SifreUretici {

    const val KUCUK = "abcdefghijklmnopqrstuvwxyz"
    const val BUYUK = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val RAKAM = "0123456789"
    const val SEMBOL = "!@#$%^&*()-_=+[]{};:,.?/"
    private const val BENZER = "O0oIl1"

    const val EN_AZ = 6
    const val EN_COK = 64

    data class Secenekler(
        val uzunluk: Int = 16,
        val kucuk: Boolean = true,
        val buyuk: Boolean = true,
        val rakam: Boolean = true,
        val sembol: Boolean = true,
        val benzerleriHaricTut: Boolean = false
    )

    fun gruplar(s: Secenekler): List<String> = buildList {
        if (s.kucuk) add(KUCUK)
        if (s.buyuk) add(BUYUK)
        if (s.rakam) add(RAKAM)
        if (s.sembol) add(SEMBOL)
    }.map { g -> if (s.benzerleriHaricTut) g.filterNot { it in BENZER } else g }
        .filter { it.isNotEmpty() }

    /**
     * Seçilen her karakter türünden en az bir karakter içeren şifre üretir.
     * @throws IllegalArgumentException hiçbir tür seçilmemişse ya da uzunluk geçersizse
     */
    fun uret(s: Secenekler, rastgele: SecureRandom = SecureRandom()): String {
        require(s.uzunluk in EN_AZ..EN_COK) { "Uzunluk $EN_AZ ile $EN_COK arasında olmalı." }
        val g = gruplar(s)
        require(g.isNotEmpty()) { "En az bir karakter türü seçin." }
        val havuz = g.joinToString("")
        val karakterler = CharArray(s.uzunluk)
        g.forEachIndexed { i, grup -> karakterler[i] = grup[rastgele.nextInt(grup.length)] }
        for (i in g.size until s.uzunluk) karakterler[i] = havuz[rastgele.nextInt(havuz.length)]
        // Fisher–Yates karıştırma: zorunlu karakterler hep başta kalmasın.
        for (i in karakterler.size - 1 downTo 1) {
            val j = rastgele.nextInt(i + 1)
            val t = karakterler[i]
            karakterler[i] = karakterler[j]
            karakterler[j] = t
        }
        return karakterler.concatToString()
    }

    /** Yaklaşık entropi (bit) = uzunluk × log2(havuz boyutu). */
    fun entropiBit(s: Secenekler): Double {
        val havuz = gruplar(s).sumOf { it.length }
        if (havuz <= 1) return 0.0
        return s.uzunluk * log2(havuz.toDouble())
    }

    fun gucEtiketi(bit: Double): String = when {
        bit < 40 -> "Zayıf"
        bit < 60 -> "Orta"
        bit < 80 -> "Güçlü"
        else -> "Çok güçlü"
    }
}
