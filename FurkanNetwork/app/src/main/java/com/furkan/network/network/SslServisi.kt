package com.furkan.network.network

import com.furkan.network.models.SertifikaBilgisi
import com.furkan.network.models.SslSonucu
import com.furkan.network.utils.HashYardimcisi
import com.furkan.network.utils.X500Ayristirici
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * HTTPS sertifika bilgisi. Android'in varsayılan güven deposu kullanılır;
 * güvenilmeyen sertifikalar kabul EDİLMEZ (güvensiz TrustManager yok), hata açıklanır.
 */
class SslServisi {

    suspend fun incele(host: String, port: Int = 443): SslSonucu = withContext(Dispatchers.IO) {
        val baslangic = System.nanoTime()
        val ham = Socket()
        try {
            ham.connect(InetSocketAddress(host, port), ZAMAN_ASIMI_MS)
            ham.soTimeout = ZAMAN_ASIMI_MS
            val fabrika = SSLSocketFactory.getDefault() as SSLSocketFactory
            // Katmanlı soket: host adı SNI olarak gönderilir.
            val ssl = fabrika.createSocket(ham, host, port, true) as SSLSocket
            ssl.use { soket ->
                soket.soTimeout = ZAMAN_ASIMI_MS
                soket.startHandshake()
                val oturum = soket.session
                val zincir = oturum.peerCertificates.filterIsInstance<X509Certificate>()
                if (zincir.isEmpty()) throw SSLPeerUnverifiedException("Sunucu sertifika göndermedi")
                val adDogru = HttpsURLConnection.getDefaultHostnameVerifier().verify(host, oturum)
                SslSonucu(
                    host = host,
                    port = port,
                    protokol = oturum.protocol ?: "?",
                    sifreTakimi = oturum.cipherSuite ?: "?",
                    adDogrulandi = adDogru,
                    sureMs = (System.nanoTime() - baslangic) / 1_000_000,
                    zincir = zincir.map(::ozetle)
                )
            }
        } finally {
            try {
                ham.close()
            } catch (_: IOException) {
            }
        }
    }

    private fun ozetle(c: X509Certificate): SertifikaBilgisi {
        val alternatif = try {
            c.subjectAlternativeNames?.mapNotNull { girdi ->
                val tur = girdi.getOrNull(0) as? Int
                val deger = girdi.getOrNull(1) as? String
                if ((tur == 2 || tur == 7) && deger != null) deger else null
            }.orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        val anahtar = when (val k = c.publicKey) {
            is RSAPublicKey -> "RSA ${k.modulus.bitLength()} bit"
            is ECPublicKey -> "EC ${k.params.curve.field.fieldSize} bit"
            else -> k.algorithm ?: "?"
        }
        val konu = c.subjectX500Principal.name
        val yayinci = c.issuerX500Principal.name
        return SertifikaBilgisi(
            konu = konu,
            konuKisa = X500Ayristirici.kisaAd(konu),
            yayinci = yayinci,
            yayinciKisa = X500Ayristirici.kisaAd(yayinci),
            baslangic = c.notBefore.time,
            bitis = c.notAfter.time,
            seriNo = c.serialNumber.toString(16).uppercase(),
            imzaAlgoritmasi = c.sigAlgName ?: "?",
            anahtar = anahtar,
            alternatifAdlar = alternatif,
            sha256ParmakIzi = HashYardimcisi.parmakIzi(c.encoded)
        )
    }

    private companion object {
        const val ZAMAN_ASIMI_MS = 8_000
    }
}
