package com.furkan.network.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PingCozumleyiciTest {

    @Test
    fun basariliYanit() {
        val cikti = """
            PING 8.8.8.8 (8.8.8.8) 56(84) bytes of data.
            64 bytes from 8.8.8.8: icmp_seq=1 ttl=117 time=12.3 ms

            --- 8.8.8.8 ping statistics ---
            1 packets transmitted, 1 received, 0% packet loss, time 0ms
            rtt min/avg/max/mdev = 12.345/12.345/12.345/0.000 ms
        """.trimIndent()
        val c = PingCiktiCozumleyici.coz(cikti)
        assertEquals(12.3, c.sureMs!!, 0.0001)
        assertEquals(117, c.ttl)
        assertEquals("8.8.8.8", c.yanitVeren)
        assertNull(c.ttlAsildiKaynak)
    }

    @Test
    fun adliVeIpv6Yanit() {
        val a = PingCiktiCozumleyici.coz("64 bytes from fra16s52-in-f14.1e100.net (142.250.185.78): icmp_seq=1 ttl=117 time=9.87 ms")
        assertEquals("142.250.185.78", a.yanitVeren)
        assertEquals(9.87, a.sureMs!!, 0.0001)
        val b = PingCiktiCozumleyici.coz("64 bytes from 2a00:1450:4001:82b::200e: icmp_seq=1 ttl=117 time<1 ms")
        assertEquals("2a00:1450:4001:82b::200e", b.yanitVeren)
        assertEquals(1.0, b.sureMs!!, 0.0001)
    }

    @Test
    fun zamanAsimi() {
        val cikti = """
            PING 10.255.255.1 (10.255.255.1) 56(84) bytes of data.

            --- 10.255.255.1 ping statistics ---
            1 packets transmitted, 0 received, 100% packet loss, time 0ms
        """.trimIndent()
        val c = PingCiktiCozumleyici.coz(cikti)
        assertNull(c.sureMs)
        assertNull(c.yanitVeren)
        assertNull(c.ttlAsildiKaynak)
    }

    @Test
    fun ttlAsildi() {
        val c1 = PingCiktiCozumleyici.coz("From 192.168.1.1 icmp_seq=1 Time to live exceeded")
        assertEquals("192.168.1.1", c1.ttlAsildiKaynak)
        val c2 = PingCiktiCozumleyici.coz("From 10.0.0.1: icmp_seq=1 Time to live exceeded")
        assertEquals("10.0.0.1", c2.ttlAsildiKaynak)
        val c3 = PingCiktiCozumleyici.coz("From router.lan (192.168.0.1) icmp_seq=1 Time to live exceeded")
        assertEquals("192.168.0.1", c3.ttlAsildiKaynak)
        assertNull(c3.sureMs)
    }

    @Test
    fun ulasilamaz() {
        val c = PingCiktiCozumleyici.coz("From 10.0.0.1 icmp_seq=1 Destination Host Unreachable")
        assertEquals("10.0.0.1", c.ulasilamazKaynak)
        assertEquals("Hedef cihaza ulaşılamıyor", c.ulasilamazNeden)
    }

    @Test
    fun kullanilamaz() {
        assertTrue(PingCiktiCozumleyici.kullanilamazMi("ping: socket: Permission denied"))
        assertTrue(PingCiktiCozumleyici.kullanilamazMi("ping: invalid option -- 'W'\nUsage: ping"))
        assertFalse(PingCiktiCozumleyici.kullanilamazMi("1 packets transmitted, 0 received"))
        assertTrue(PingCiktiCozumleyici.agErisilemezMi("connect: Network is unreachable"))
    }

    @Test
    fun istatistik() {
        val s = PingIstatistigi.hesapla(listOf(10.0, null, 20.0, 30.0))
        assertEquals(4, s.gonderilen)
        assertEquals(3, s.basarili)
        assertEquals(1, s.basarisiz)
        assertEquals(25.0, s.kayipYuzde, 0.0001)
        assertEquals(10.0, s.enAz!!, 0.0001)
        assertEquals(20.0, s.ortalama!!, 0.0001)
        assertEquals(30.0, s.enCok!!, 0.0001)
        assertEquals(10.0, s.jitter!!, 0.0001)
        val bos = PingIstatistigi.hesapla(listOf(null, null))
        assertEquals(100.0, bos.kayipYuzde, 0.0001)
        assertNull(bos.ortalama)
    }
}
