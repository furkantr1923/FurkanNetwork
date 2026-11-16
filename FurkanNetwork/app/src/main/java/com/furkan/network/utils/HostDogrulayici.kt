package com.furkan.network.utils

import java.net.IDN

/**
 * Kullanıcının yazdığı adresi temizler ve doğrular.
 * "https://www.Google.com/arama?q=1" → "www.google.com"
 * "örnek.com.tr" → "xn--rnek-zoa.com.tr" (IDN)
 * "[2001:db8::1]:443" → "2001:db8::1", port 443
 */
object HostDogrulayici {

    enum class Tur { IPV4, IPV6, ALAN_ADI }

    sealed interface Sonuc {
        data class Gecerli(val host: String, val port: Int?, val tur: Tur) : Sonuc
        data class Gecersiz(val neden: String) : Sonuc
    }

    private val SEMA = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://")

    fun ayikla(girdi: String): Sonuc {
        var s = girdi.trim()
        if (s.isEmpty()) return Sonuc.Gecersiz("Lütfen bir alan adı veya IP adresi girin.")
        if (s.any { it.isWhitespace() }) return Sonuc.Gecersiz("Adres boşluk içeremez.")
        s = s.replace(SEMA, "")
        s = s.substringBefore('/').substringBefore('?').substringBefore('#')
        s = s.substringAfterLast('@')
        if (s.isEmpty()) return Sonuc.Gecersiz("Adres boş görünüyor.")

        // [IPv6]:port
        if (s.startsWith("[")) {
            val kapanis = s.indexOf(']')
            if (kapanis < 0) return Sonuc.Gecersiz("IPv6 adresinde ']' eksik.")
            val ic = s.substring(1, kapanis)
            val kalan = s.substring(kapanis + 1)
            var port: Int? = null
            if (kalan.isNotEmpty()) {
                if (!kalan.startsWith(":")) return Sonuc.Gecersiz("Geçersiz adres biçimi.")
                port = portAyristir(kalan.substring(1))
                    ?: return Sonuc.Gecersiz("Geçersiz port numarası (1–65535).")
            }
            return if (IpAdresi.ipv6Mi(ic)) Sonuc.Gecerli(IpAdresi.kapsamsiz(ic).lowercase(), port, Tur.IPV6)
            else Sonuc.Gecersiz("Geçersiz IPv6 adresi.")
        }

        val ikiNoktaSayisi = s.count { it == ':' }
        if (ikiNoktaSayisi > 1) {
            return if (IpAdresi.ipv6Mi(s)) Sonuc.Gecerli(IpAdresi.kapsamsiz(s).lowercase(), null, Tur.IPV6)
            else Sonuc.Gecersiz("Geçersiz IPv6 adresi.")
        }
        var port: Int? = null
        if (ikiNoktaSayisi == 1) {
            port = portAyristir(s.substringAfter(':'))
                ?: return Sonuc.Gecersiz("Geçersiz port numarası (1–65535).")
            s = s.substringBefore(':')
        }
        s = s.trimEnd('.')
        if (s.isEmpty()) return Sonuc.Gecersiz("Adres boş görünüyor.")

        if (s.all { it in '0'..'9' || it == '.' }) {
            return if (IpAdresi.ipv4Mu(s)) Sonuc.Gecerli(s, port, Tur.IPV4)
            else Sonuc.Gecersiz("Geçersiz IPv4 adresi. Her bölüm 0–255 arasında olmalı.")
        }

        val ascii = try {
            IDN.toASCII(s.lowercase(), IDN.ALLOW_UNASSIGNED)
        } catch (e: IllegalArgumentException) {
            return Sonuc.Gecersiz("Geçersiz alan adı.")
        }
        if (ascii.isEmpty() || ascii.length > 253) return Sonuc.Gecersiz("Alan adı uzunluğu geçersiz.")
        if (!ascii.split('.').all(::etiketGecerli)) {
            return Sonuc.Gecersiz("Geçersiz alan adı. Örnek: google.com")
        }
        return Sonuc.Gecerli(ascii, port, Tur.ALAN_ADI)
    }

    fun portAyristir(metin: String): Int? {
        if (metin.isEmpty() || metin.length > 5 || !metin.all { it in '0'..'9' }) return null
        return metin.toInt().takeIf { it in 1..65535 }
    }

    private fun etiketGecerli(e: String): Boolean =
        e.length in 1..63 &&
            e.all { it in 'a'..'z' || it in '0'..'9' || it == '-' || it == '_' } &&
            !e.startsWith('-') && !e.endsWith('-')
}
