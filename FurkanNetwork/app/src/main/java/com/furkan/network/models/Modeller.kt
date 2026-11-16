package com.furkan.network.models

/** Etkin bağlantının türü. */
enum class BaglantiTuru(val etiket: String) {
    WIFI("Wi-Fi"),
    MOBIL("Mobil veri"),
    ETHERNET("Ethernet"),
    VPN("VPN"),
    DIGER("Diğer"),
    YOK("Bağlantı yok")
}

/** Android'in bildirdiği anlık ağ durumu. Uydurma veri içermez; bilinmeyen alanlar boştur. */
data class AgDurumu(
    val bagli: Boolean,
    /** Android internet erişimini doğruladı mı (NET_CAPABILITY_VALIDATED). */
    val internetDogrulandi: Boolean,
    val tur: BaglantiTuru,
    val vpnAktif: Boolean,
    val olcumlu: Boolean,
    val yerelIpv4: List<String>,
    val yerelIpv6: List<String>,
    val agGecidi: String?,
    val dnsSunuculari: List<String>,
    val ozelDns: String?,
    val arayuzAdi: String?,
    /** Android'in tahmini bant genişliği (kbps); gerçek ölçüm değildir. */
    val tahminiIndirmeKbps: Int?,
    val tahminiYuklemeKbps: Int?
) {
    /** Ağ değişimini algılamak için kısa kimlik. */
    val kimlik: String get() = "${tur.name}|${yerelIpv4.joinToString()}|${yerelIpv6.firstOrNull().orEmpty()}"

    companion object {
        val BAGLANTI_YOK = AgDurumu(
            bagli = false, internetDogrulandi = false, tur = BaglantiTuru.YOK, vpnAktif = false,
            olcumlu = false, yerelIpv4 = emptyList(), yerelIpv6 = emptyList(), agGecidi = null,
            dnsSunuculari = emptyList(), ozelDns = null, arayuzAdi = null,
            tahminiIndirmeKbps = null, tahminiYuklemeKbps = null
        )
    }
}

/** Wi-Fi / mobil ayrıntıları. Erişilemeyen bilgi null'dır ve nedeni [notlar]da yazar. */
data class KablosuzBilgisi(
    val wifiAcik: Boolean?,
    val wifiBagli: Boolean,
    val ssid: String?,
    val rssi: Int?,
    val baglantiHiziMbps: Int?,
    val frekansMhz: Int?,
    val mobilBagli: Boolean,
    val mobilVeriAcik: Boolean?,
    val operator: String?,
    val mobilSinyalSeviyesi: Int?,
    val simVar: Boolean?,
    val telefonDestegi: Boolean,
    val notlar: List<String>
)

sealed interface GenelIpDurumu {
    data object Bos : GenelIpDurumu
    data object Yukleniyor : GenelIpDurumu
    data class Basarili(val ipv4: String?, val ipv6: String?, val zaman: Long) : GenelIpDurumu
    data class Hata(val mesaj: String) : GenelIpDurumu
}

data class DnsKayitSonucu(
    val adresler: List<String>,
    val sureMs: Long,
    val hata: String?
)

data class DnsSonucu(
    val alanAdi: String,
    val ipv4: DnsKayitSonucu,
    val ipv6: DnsKayitSonucu,
    val toplamSureMs: Long,
    val yontem: String
)

enum class PingYontemi(val etiket: String) {
    ICMP("ICMP (sistem ping)"),
    TCP("TCP 443 bağlantı süresi")
}

data class PingOrnegi(
    val sira: Int,
    val sureMs: Double?,
    val ttl: Int?,
    val not: String?
)

data class TraceAtlama(
    val sira: Int,
    val adres: String?,
    val sureMs: Double?,
    /** false ise süre, işlem başlatma süresini de içeren yaklaşık değerdir. */
    val kesinSure: Boolean,
    val hedef: Boolean,
    val not: String?
)

data class SertifikaBilgisi(
    val konu: String,
    val konuKisa: String,
    val yayinci: String,
    val yayinciKisa: String,
    val baslangic: Long,
    val bitis: Long,
    val seriNo: String,
    val imzaAlgoritmasi: String,
    val anahtar: String,
    val alternatifAdlar: List<String>,
    val sha256ParmakIzi: String
)

data class SslSonucu(
    val host: String,
    val port: Int,
    val protokol: String,
    val sifreTakimi: String,
    val adDogrulandi: Boolean,
    val sureMs: Long,
    val zincir: List<SertifikaBilgisi>
)

data class HizOlcumu(
    val indirilenBayt: Long,
    val hedefBayt: Long,
    val gecenMs: Long,
    val anlikMbps: Double,
    val ortalamaMbps: Double,
    val ilkBaytMs: Long?
)

/** Geçmiş kaydı türleri. [kod] dosyada saklanan kalıcı değerdir. */
enum class TestTuru(val kod: String, val etiket: String) {
    IP("ip", "IP Bilgisi"),
    DNS("dns", "DNS"),
    PING("ping", "Ping"),
    TRACEROUTE("trace", "Traceroute"),
    AG("ag", "Ağ Bilgisi"),
    SSL("ssl", "SSL"),
    HIZ("hiz", "Hız Testi");

    companion object {
        fun koddan(kod: String): TestTuru = entries.firstOrNull { it.kod == kod } ?: AG
    }
}

data class GecmisKaydi(
    val id: Long,
    val tur: TestTuru,
    val baslik: String,
    val ozet: String,
    val ayrinti: String,
    val zaman: Long
)

data class Ayarlar(
    val koyuTema: Boolean = true,
    val genelIpOtomatik: Boolean = true,
    val gecmisKaydet: Boolean = true,
    val pingAdet: Int = 10
)
