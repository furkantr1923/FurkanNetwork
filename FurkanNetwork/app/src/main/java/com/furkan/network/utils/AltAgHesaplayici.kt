package com.furkan.network.utils

/**
 * IPv4 alt ağ hesaplayıcı. Tamamen çevrimdışı çalışır.
 * Girdi örnekleri: "192.168.1.10/24", ya da IP + "255.255.255.0" / "24" / "/24".
 */
object AltAgHesaplayici {

    data class Sonuc(
        val ip: String,
        val onEk: Int,
        val maske: String,
        val joker: String,
        val agAdresi: String,
        val yayinAdresi: String,
        val ilkHost: String,
        val sonHost: String,
        val toplamAdres: Long,
        val kullanilabilirHost: Long,
        val sinif: String,
        val tur: String,
        val ipIkili: String,
        val maskeIkili: String
    )

    sealed interface Cikti {
        data class Basarili(val sonuc: Sonuc) : Cikti
        data class Hata(val mesaj: String) : Cikti
    }

    fun hesapla(ipGirdisi: String, maskeGirdisi: String = ""): Cikti {
        val temiz = ipGirdisi.trim()
        val ipMetni: String
        val onEkMetni: String
        if (temiz.contains('/')) {
            ipMetni = temiz.substringBefore('/').trim()
            onEkMetni = temiz.substringAfter('/').trim()
        } else {
            ipMetni = temiz
            onEkMetni = maskeGirdisi.trim().removePrefix("/").trim()
        }
        if (ipMetni.isEmpty()) return Cikti.Hata("Bir IPv4 adresi girin. Örnek: 192.168.1.10/24")
        val ip = IpAdresi.ipv4Ayristir(ipMetni)
            ?: return Cikti.Hata("Geçersiz IPv4 adresi: \"$ipMetni\". Her bölüm 0–255 arasında olmalı.")

        val onEk: Int = when {
            onEkMetni.isEmpty() ->
                return Cikti.Hata("Alt ağ önekini (ör. /24) veya maskeyi (ör. 255.255.255.0) girin.")
            onEkMetni.all { it in '0'..'9' } -> {
                val n = onEkMetni.toIntOrNull()
                if (n == null || n !in 0..32) return Cikti.Hata("Önek 0 ile 32 arasında olmalı.")
                n
            }
            else -> {
                val m = IpAdresi.ipv4Ayristir(onEkMetni)
                    ?: return Cikti.Hata("Geçersiz alt ağ maskesi: \"$onEkMetni\".")
                maskeOnEki(m)
                    ?: return Cikti.Hata("Maske ardışık 1 bitlerinden oluşmalı (ör. 255.255.255.0).")
            }
        }

        val maske = onEkdenMaske(onEk)
        val joker = maske.inv() and 0xFFFFFFFFL
        val ag = ip and maske
        val yayin = ag or joker
        val toplam = 1L shl (32 - onEk)
        val kullanilabilir = when (onEk) {
            32 -> 1L
            31 -> 2L
            else -> toplam - 2
        }
        val ilk: Long
        val son: Long
        when (onEk) {
            32 -> { ilk = ip; son = ip }
            31 -> { ilk = ag; son = yayin }
            else -> { ilk = ag + 1; son = yayin - 1 }
        }

        return Cikti.Basarili(
            Sonuc(
                ip = IpAdresi.ipv4Metni(ip),
                onEk = onEk,
                maske = IpAdresi.ipv4Metni(maske),
                joker = IpAdresi.ipv4Metni(joker),
                agAdresi = IpAdresi.ipv4Metni(ag),
                yayinAdresi = IpAdresi.ipv4Metni(yayin),
                ilkHost = IpAdresi.ipv4Metni(ilk),
                sonHost = IpAdresi.ipv4Metni(son),
                toplamAdres = toplam,
                kullanilabilirHost = kullanilabilir,
                sinif = sinif(ip),
                tur = adresTuru(ip),
                ipIkili = IpAdresi.ipv4Ikili(ip),
                maskeIkili = IpAdresi.ipv4Ikili(maske)
            )
        )
    }

    fun onEkdenMaske(onEk: Int): Long =
        if (onEk == 0) 0L else (0xFFFFFFFFL shl (32 - onEk)) and 0xFFFFFFFFL

    /** Ardışık maske ise önek uzunluğunu döndürür, değilse null. */
    fun maskeOnEki(maske: Long): Int? {
        val ters = maske.inv() and 0xFFFFFFFFL
        if (((ters + 1) and ters) != 0L) return null
        return 32 - java.lang.Long.bitCount(ters)
    }

    fun sinif(ip: Long): String {
        val ilk = ((ip shr 24) and 0xFF).toInt()
        return when (ilk) {
            in 0..127 -> "A"
            in 128..191 -> "B"
            in 192..223 -> "C"
            in 224..239 -> "D (çok noktaya yayın)"
            else -> "E (deneysel)"
        }
    }

    fun adresTuru(ip: Long): String {
        fun icinde(ag: String, onEk: Int): Boolean {
            val a = IpAdresi.ipv4Ayristir(ag) ?: return false
            val m = onEkdenMaske(onEk)
            return (ip and m) == (a and m)
        }
        return when {
            icinde("0.0.0.0", 8) -> "Bu ağ (0.0.0.0/8)"
            icinde("10.0.0.0", 8) || icinde("172.16.0.0", 12) || icinde("192.168.0.0", 16) -> "Özel (RFC 1918)"
            icinde("127.0.0.0", 8) -> "Geri döngü (loopback)"
            icinde("169.254.0.0", 16) -> "Bağlantı-yerel (APIPA)"
            icinde("100.64.0.0", 10) -> "Paylaşımlı adres alanı / CGNAT (RFC 6598)"
            icinde("224.0.0.0", 4) -> "Çok noktaya yayın (multicast)"
            icinde("240.0.0.0", 4) -> "Ayrılmış"
            else -> "Genel (internet)"
        }
    }
}
