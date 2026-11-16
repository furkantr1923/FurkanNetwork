package com.furkan.network.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AraclarTest {

    @Test
    fun sha256BilinenDegerler() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            HashYardimcisi.sha256Hex("abc")
        )
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            HashYardimcisi.sha256Hex("")
        )
        val (akisHash, boyut) = HashYardimcisi.sha256Akis("abc".byteInputStream())
        assertEquals(HashYardimcisi.sha256Hex("abc"), akisHash)
        assertEquals(3L, boyut)
        assertTrue(HashYardimcisi.esitMi("BA:78:16:bf", "ba7816BF"))
        assertFalse(HashYardimcisi.esitMi("", ""))
    }

    @Test
    fun sifreKurallari() {
        val s = SifreUretici.Secenekler(uzunluk = 24, sembol = false, benzerleriHaricTut = true)
        repeat(200) {
            val p = SifreUretici.uret(s)
            assertEquals(24, p.length)
            assertTrue(p.any { it in SifreUretici.KUCUK })
            assertTrue(p.any { it in SifreUretici.BUYUK })
            assertTrue(p.any { it in SifreUretici.RAKAM })
            assertFalse(p.any { it in SifreUretici.SEMBOL })
            assertFalse(p.any { it in "O0oIl1" })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun sifreTurSecilmedi() {
        SifreUretici.uret(SifreUretici.Secenekler(kucuk = false, buyuk = false, rakam = false, sembol = false))
    }

    @Test
    fun entropi() {
        val bit = SifreUretici.entropiBit(SifreUretici.Secenekler(uzunluk = 16))
        assertTrue(bit > 100)
        assertEquals("Çok güçlü", SifreUretici.gucEtiketi(bit))
    }

    @Test
    fun sinyal() {
        assertEquals(4, SinyalYardimcisi.seviye(-50))
        assertEquals(2, SinyalYardimcisi.seviye(-70))
        assertEquals(0, SinyalYardimcisi.seviye(-95))
        assertNull(SinyalYardimcisi.seviye(-127))
        assertEquals("5 GHz", SinyalYardimcisi.bant(5180))
        assertEquals(36, SinyalYardimcisi.kanal(5180))
        assertEquals(6, SinyalYardimcisi.kanal(2437))
        assertEquals(14, SinyalYardimcisi.kanal(2484))
    }

    @Test
    fun x500() {
        val ad = "CN=*.google.com,O=Google LLC,C=US"
        assertEquals("*.google.com", X500Ayristirici.alan(ad, "cn"))
        assertEquals("Google LLC", X500Ayristirici.alan(ad, "O"))
        assertEquals("Acme, Inc.", X500Ayristirici.alan("CN=a,O=Acme\\, Inc.", "O"))
        assertEquals("Google Trust Services", X500Ayristirici.kisaAd("O=Google Trust Services,C=US"))
    }

    @Test
    fun bicim() {
        assertEquals("12,3 ms", Bicim.ms(12.34))
        assertEquals("%25,0", Bicim.yuzde(25.0))
        assertEquals("12,5 MB", Bicim.bayt(12_500_000))
    }
}
