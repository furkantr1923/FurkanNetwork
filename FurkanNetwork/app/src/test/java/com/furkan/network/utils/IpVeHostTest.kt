package com.furkan.network.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IpVeHostTest {

    @Test
    fun ipv4() {
        assertEquals(3232235786L, IpAdresi.ipv4Ayristir("192.168.1.10"))
        assertNull(IpAdresi.ipv4Ayristir("256.1.1.1"))
        assertNull(IpAdresi.ipv4Ayristir("1.2.3"))
        assertNull(IpAdresi.ipv4Ayristir("1.2.3.4.5"))
        assertNull(IpAdresi.ipv4Ayristir("1..3.4"))
        assertNull(IpAdresi.ipv4Ayristir("a.b.c.d"))
        assertEquals("192.168.1.10", IpAdresi.ipv4Metni(3232235786L))
        assertEquals("11000000.10101000.00000001.00001010", IpAdresi.ipv4Ikili(3232235786L))
    }

    @Test
    fun ipv6() {
        listOf("::1", "::", "2001:db8::1", "fe80::1%wlan0", "::ffff:192.168.1.1",
            "2001:0db8:0000:0000:0000:ff00:0042:8329", "2a00:1450:4001:82b::200e")
            .forEach { assertTrue(it, IpAdresi.ipv6Mi(it)) }
        listOf("1::2::3", ":::", "2001:db8:::1", "12345::1", "g::1", "1:2:3:4:5:6:7",
            "1:2:3:4:5:6:7:8:9", "::ffff:999.1.1.1", "abc")
            .forEach { assertFalse(it, IpAdresi.ipv6Mi(it)) }
    }

    private fun gecerli(s: String) = HostDogrulayici.ayikla(s) as HostDogrulayici.Sonuc.Gecerli

    @Test
    fun hostTemizleme() {
        assertEquals("www.google.com", gecerli("https://www.Google.com/arama?q=1").host)
        assertEquals("google.com", gecerli("  google.com.  ").host)
        assertEquals("example.com", gecerli("user@example.com").host)
        val p = gecerli("example.com:8443")
        assertEquals("example.com", p.host)
        assertEquals(8443, p.port)
        val v6 = gecerli("[2001:db8::1]:443")
        assertEquals("2001:db8::1", v6.host)
        assertEquals(443, v6.port)
        assertEquals(HostDogrulayici.Tur.IPV6, v6.tur)
        assertEquals(HostDogrulayici.Tur.IPV4, gecerli("8.8.8.8").tur)
        assertTrue(gecerli("örnek.com.tr").host.startsWith("xn--"))
    }

    @Test
    fun hostHatalari() {
        listOf("", "   ", "goo gle.com", "999.1.1.1", "-abc.com", "abc-.com",
            "example.com:0", "example.com:70000", "a..b", "ex!ample.com", "[::1")
            .forEach { assertTrue(it, HostDogrulayici.ayikla(it) is HostDogrulayici.Sonuc.Gecersiz) }
    }
}
