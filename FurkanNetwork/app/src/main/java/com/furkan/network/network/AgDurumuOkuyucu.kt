package com.furkan.network.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.furkan.network.models.AgDurumu
import com.furkan.network.models.BaglantiTuru
import com.furkan.network.utils.IpAdresi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.Inet4Address
import java.net.Inet6Address

/**
 * ConnectivityManager üzerinden bağlantı türü, IP adresleri, ağ geçidi ve DNS
 * sunucularını okur. Yalnızca ACCESS_NETWORK_STATE izni gerekir.
 */
class AgDurumuOkuyucu(context: Context) {

    private val cm: ConnectivityManager? = context.getSystemService(ConnectivityManager::class.java)

    /** Anlık durum (tek seferlik okuma). */
    fun anlik(): AgDurumu {
        val yonetici = cm ?: return AgDurumu.BAGLANTI_YOK
        return try {
            val ag = yonetici.activeNetwork ?: return AgDurumu.BAGLANTI_YOK
            olustur(yonetici.getNetworkCapabilities(ag), yonetici.getLinkProperties(ag))
        } catch (e: Exception) {
            AgDurumu.BAGLANTI_YOK
        }
    }

    /**
     * Varsayılan ağ değiştikçe yeni durum yayar. Toplayıcı durduğunda
     * (ekran kapandığında) geri çağrı otomatik olarak kaldırılır.
     */
    fun akis(): Flow<AgDurumu> = callbackFlow {
        val yonetici = cm
        trySend(anlik())
        if (yonetici == null) {
            awaitClose { }
            return@callbackFlow
        }
        var sonYetenek: NetworkCapabilities? = null
        var sonBaglanti: LinkProperties? = null
        val geriCagri = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                sonYetenek = networkCapabilities
                trySend(olustur(sonYetenek, sonBaglanti ?: guvenliBaglanti(yonetici, network)))
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                sonBaglanti = linkProperties
                trySend(olustur(sonYetenek ?: guvenliYetenek(yonetici, network), sonBaglanti))
            }

            override fun onLost(network: Network) {
                sonYetenek = null
                sonBaglanti = null
                trySend(AgDurumu.BAGLANTI_YOK)
            }
        }
        val kaydedildi = try {
            yonetici.registerDefaultNetworkCallback(geriCagri)
            true
        } catch (e: Exception) {
            false
        }
        awaitClose {
            if (kaydedildi) {
                try {
                    yonetici.unregisterNetworkCallback(geriCagri)
                } catch (_: Exception) {
                }
            }
        }
    }.conflate().distinctUntilChanged()

    private fun guvenliBaglanti(y: ConnectivityManager, n: Network): LinkProperties? =
        try { y.getLinkProperties(n) } catch (e: Exception) { null }

    private fun guvenliYetenek(y: ConnectivityManager, n: Network): NetworkCapabilities? =
        try { y.getNetworkCapabilities(n) } catch (e: Exception) { null }

    private fun olustur(yetenek: NetworkCapabilities?, baglanti: LinkProperties?): AgDurumu {
        if (yetenek == null && baglanti == null) return AgDurumu.BAGLANTI_YOK

        val vpn = yetenek?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        val tur = when {
            yetenek == null -> BaglantiTuru.DIGER
            yetenek.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> BaglantiTuru.WIFI
            yetenek.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> BaglantiTuru.MOBIL
            yetenek.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> BaglantiTuru.ETHERNET
            vpn -> BaglantiTuru.VPN
            else -> BaglantiTuru.DIGER
        }

        val ipv4 = mutableListOf<String>()
        val ipv6 = mutableListOf<String>()
        baglanti?.linkAddresses?.forEach { la ->
            val adres = la.address
            if (adres.isLoopbackAddress) return@forEach
            val metin = adres.hostAddress?.let(IpAdresi::kapsamsiz) ?: return@forEach
            when (adres) {
                is Inet4Address -> ipv4 += metin
                is Inet6Address -> ipv6 += metin
            }
        }
        // Genel (global) IPv6 adresleri önce, bağlantı-yerel olanlar sonra.
        ipv6.sortBy { if (IpAdresi.ipv6BaglantiYerelMi(it)) 1 else 0 }

        val gecit = baglanti?.routes
            ?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address && it.gateway?.isAnyLocalAddress == false }
            ?.gateway?.hostAddress
            ?: baglanti?.routes
                ?.firstOrNull { it.isDefaultRoute && it.gateway is Inet6Address && it.gateway?.isAnyLocalAddress == false }
                ?.gateway?.hostAddress?.let(IpAdresi::kapsamsiz)

        val dns = baglanti?.dnsServers?.mapNotNull { it.hostAddress?.let(IpAdresi::kapsamsiz) }.orEmpty()

        val ozelDns = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && baglanti != null) {
            when {
                baglanti.privateDnsServerName != null -> "Etkin: ${baglanti.privateDnsServerName}"
                baglanti.isPrivateDnsActive -> "Etkin (otomatik)"
                else -> "Kapalı"
            }
        } else null

        return AgDurumu(
            bagli = true,
            internetDogrulandi = yetenek?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
            tur = tur,
            vpnAktif = vpn,
            olcumlu = yetenek?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) != true,
            yerelIpv4 = ipv4,
            yerelIpv6 = ipv6,
            agGecidi = gecit,
            dnsSunuculari = dns,
            ozelDns = ozelDns,
            arayuzAdi = baglanti?.interfaceName,
            tahminiIndirmeKbps = yetenek?.linkDownstreamBandwidthKbps?.takeIf { it > 0 },
            tahminiYuklemeKbps = yetenek?.linkUpstreamBandwidthKbps?.takeIf { it > 0 }
        )
    }
}
