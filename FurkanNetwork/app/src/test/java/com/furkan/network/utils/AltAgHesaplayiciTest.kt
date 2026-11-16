package com.furkan.network.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AltAgHesaplayiciTest {

    private fun basarili(ip: String, maske: String = ""): AltAgHesaplayici.Sonuc {
        val c = AltAgHesaplayici.hesapla(ip, maske)
        assertTrue("Beklenen başarı, gelen: $c", c is AltAgHesaplayici.Cikti.Basarili)
        return (c as AltAgHesaplayici.Cikti.Basarili).sonuc
    }

    @Test
    fun cidr24() {
        val s = basarili("192.168.1.10/24")
        assertEquals("255.255.255.0", s.maske)
        assertEquals("0.0.0.255", s.joker)
        assertEquals("192.168.1.0", s.agAdresi)
        assertEquals("192.168.1.255", s.yayinAdresi)
        assertEquals("192.168.1.1", s.ilkHost)
        assertEquals("192.168.1.254", s.sonHost)
        assertEquals(256L, s.toplamAdres)
        assertEquals(254L, s.kullanilabilirHost)
        assertEquals("C", s.sinif)
        assertEquals("Özel (RFC 1918)", s.tur)
    }

    @Test
    fun noktaliMaske() {
        val s = basarili("10.20.30.40", "255.255.240.0")
        assertEquals(20, s.onEk)
        assertEquals("10.20.16.0", s.agAdresi)
        assertEquals("10.20.31.255", s.yayinAdresi)
        assertEquals(4094L, s.kullanilabilirHost)
    }

    @Test
    fun onEk31ve32() {
        val s31 = basarili("203.0.113.6/31")
        assertEquals(2L, s31.kullanilabilirHost)
        assertEquals("203.0.113.6", s31.ilkHost)
        assertEquals("203.0.113.7", s31.sonHost)
        val s32 = basarili("8.8.8.8/32")
        assertEquals(1L, s32.kullanilabilirHost)
        assertEquals("8.8.8.8", s32.ilkHost)
        assertEquals("Genel (internet)", s32.tur)
    }

    @Test
    fun onEk0() {
        val s = basarili("1.2.3.4/0")
        assertEquals("0.0.0.0", s.maske)
        assertEquals(4294967296L, s.toplamAdres)
    }

    @Test
    fun hatalar() {
        assertTrue(AltAgHesaplayici.hesapla("300.1.1.1/24") is AltAgHesaplayici.Cikti.Hata)
        assertTrue(AltAgHesaplayici.hesapla("192.168.1.1/33") is AltAgHesaplayici.Cikti.Hata)
        assertTrue(AltAgHesaplayici.hesapla("192.168.1.1", "255.0.255.0") is AltAgHesaplayici.Cikti.Hata)
        assertTrue(AltAgHesaplayici.hesapla("192.168.1.1") is AltAgHesaplayici.Cikti.Hata)
        assertTrue(AltAgHesaplayici.hesapla("") is AltAgHesaplayici.Cikti.Hata)
    }

    @Test
    fun ozelAdresler() {
        assertEquals("Paylaşımlı adres alanı / CGNAT (RFC 6598)", basarili("100.70.1.1/10").tur)
        assertEquals("Geri döngü (loopback)", basarili("127.0.0.1/8").tur)
        assertEquals("Bağlantı-yerel (APIPA)", basarili("169.254.3.4/16").tur)
        assertEquals("Özel (RFC 1918)", basarili("172.31.255.1/12").tur)
        assertEquals("Genel (internet)", basarili("172.32.0.1/12").tur)
    }
}
