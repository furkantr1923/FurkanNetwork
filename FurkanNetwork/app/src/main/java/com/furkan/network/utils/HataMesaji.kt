package com.furkan.network.utils

import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/** Kullanıcıya doğrudan gösterilecek, Türkçe açıklamalı hata. */
class KullaniciHatasi(mesaj: String) : Exception(mesaj)

/** Teknik istisnaları anlaşılır Türkçe açıklamalara çevirir. */
object HataMesaji {

    fun cevir(e: Throwable): String = when (e) {
        is KullaniciHatasi -> e.message ?: "İşlem tamamlanamadı."
        is UnknownHostException ->
            "Adres çözümlenemedi. Alan adını ve internet bağlantınızı kontrol edin."
        is SocketTimeoutException ->
            "Zaman aşımı: sunucu belirlenen sürede yanıt vermedi."
        is NoRouteToHostException ->
            "Hedefe ağ yolu bulunamadı (yönlendirme yok)."
        is PortUnreachableException ->
            "Hedef port erişilemez."
        is ConnectException ->
            "Bağlantı kurulamadı: sunucu bağlantıyı reddetti veya erişilemiyor."
        is SSLPeerUnverifiedException ->
            "Sunucunun kimliği doğrulanamadı (sertifika eksik veya ad uyuşmuyor)."
        is SSLHandshakeException ->
            "Güvenli bağlantı kurulamadı: sertifika güvenilir değil, süresi dolmuş " +
                "veya protokol desteklenmiyor." + ayrinti(e)
        is SSLException ->
            "SSL/TLS hatası." + ayrinti(e)
        is SocketException ->
            if (e.message?.contains("unreachable", ignoreCase = true) == true)
                "Ağa erişilemiyor. İnternet bağlantınızı kontrol edin."
            else "Bağlantı hatası." + ayrinti(e)
        is FileNotFoundException -> "Dosya bulunamadı veya açılamadı."
        is IOException -> "Ağ/G-Ç hatası." + ayrinti(e)
        is SecurityException -> "Bu işlem için gerekli Android izni yok."
        is IllegalArgumentException -> e.message ?: "Geçersiz değer."
        else -> "Beklenmeyen bir hata oluştu (${e.javaClass.simpleName})."
    }

    private fun ayrinti(e: Throwable): String {
        val m = (e.cause?.message ?: e.message)?.take(160)
        return if (m.isNullOrBlank()) "" else "\nAyrıntı: $m"
    }
}
