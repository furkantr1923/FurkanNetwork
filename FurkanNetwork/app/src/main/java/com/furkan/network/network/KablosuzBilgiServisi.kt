package com.furkan.network.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.furkan.network.models.KablosuzBilgisi

/**
 * Wi-Fi ve mobil ağ ayrıntıları. Android gizlilik kurallarına uyar:
 *  - Wi-Fi ağ adı (SSID) yalnızca konum izni verilmiş ve konum açıksa okunabilir.
 *  - Mobil ağ türü (4G/5G) READ_PHONE_STATE ister; bu izin istenmez, gösterilmez.
 */
class KablosuzBilgiServisi(context: Context) {

    private val uygulama = context.applicationContext
    private val wifi: WifiManager? = uygulama.getSystemService(WifiManager::class.java)
    private val cm: ConnectivityManager? = uygulama.getSystemService(ConnectivityManager::class.java)
    private val telefon: TelephonyManager? = uygulama.getSystemService(TelephonyManager::class.java)
    private val telefonDestegi = uygulama.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    fun konumIzniVar(): Boolean =
        ContextCompat.checkSelfPermission(uygulama, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun konumAcik(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return true
        val lm = uygulama.getSystemService(LocationManager::class.java) ?: return false
        return try { lm.isLocationEnabled } catch (e: Exception) { false }
    }

    fun oku(): KablosuzBilgisi {
        val notlar = mutableListOf<String>()
        val yetenek = try {
            cm?.let { y -> y.activeNetwork?.let { ag -> y.getNetworkCapabilities(ag) } }
        } catch (e: Exception) {
            null
        }
        val wifiBagli = yetenek?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val mobilBagli = yetenek?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        val wifiAcik = try { wifi?.isWifiEnabled } catch (e: Exception) { null }

        var ssid: String? = null
        var rssi: Int? = null
        var hiz: Int? = null
        var frekans: Int? = null
        if (wifiBagli) {
            val bilgi = wifiBilgisi(yetenek)
            if (bilgi != null) {
                rssi = bilgi.rssi.takeIf { it > -127 && it < 0 }
                hiz = bilgi.linkSpeed.takeIf { it > 0 }
                frekans = bilgi.frequency.takeIf { it > 0 }
            }
            ssid = ssidOku()
            if (ssid == null) {
                notlar += when {
                    !konumIzniVar() -> "Wi-Fi ağ adı (SSID) gizli: Android bunu yalnızca konum izniyle gösterir."
                    !konumAcik() -> "Wi-Fi ağ adı (SSID) gizli: cihazın konum servisi kapalı."
                    else -> "Wi-Fi ağ adı (SSID) bu cihazda okunamadı."
                }
            }
            if (rssi == null) notlar += "Wi-Fi sinyal gücü bu cihazda okunamadı."
        }

        var mobilVeriAcik: Boolean? = null
        var operator: String? = null
        var mobilSeviye: Int? = null
        var simVar: Boolean? = null
        if (telefonDestegi && telefon != null) {
            simVar = try {
                telefon.simState != TelephonyManager.SIM_STATE_ABSENT
            } catch (e: Exception) {
                null
            }
            mobilVeriAcik = try { telefon.isDataEnabled } catch (e: SecurityException) { null } catch (e: Exception) { null }
            operator = try { telefon.networkOperatorName?.takeIf { it.isNotBlank() } } catch (e: Exception) { null }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mobilSeviye = try { telefon.signalStrength?.level } catch (e: SecurityException) { null } catch (e: Exception) { null }
            }
            if (mobilBagli && mobilSeviye == null) notlar += "Mobil sinyal seviyesi bu cihazda/Android sürümünde okunamadı."
        } else {
            notlar += "Bu cihazda mobil ağ (telefon) donanımı yok."
        }

        return KablosuzBilgisi(
            wifiAcik = wifiAcik,
            wifiBagli = wifiBagli,
            ssid = ssid,
            rssi = rssi,
            baglantiHiziMbps = hiz,
            frekansMhz = frekans,
            mobilBagli = mobilBagli,
            mobilVeriAcik = mobilVeriAcik,
            operator = operator,
            mobilSinyalSeviyesi = mobilSeviye,
            simVar = simVar,
            telefonDestegi = telefonDestegi,
            notlar = notlar
        )
    }

    @Suppress("DEPRECATION")
    private fun wifiBilgisi(yetenek: NetworkCapabilities?): WifiInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val tasima = yetenek?.transportInfo
            if (tasima is WifiInfo) return tasima
        }
        return try { wifi?.connectionInfo } catch (e: Exception) { null }
    }

    @Suppress("DEPRECATION")
    private fun ssidOku(): String? {
        if (!konumIzniVar()) return null
        val ham = (try { wifi?.connectionInfo?.ssid } catch (e: Exception) { null }) ?: return null
        val temiz = ham.removeSurrounding("\"")
        return temiz.takeIf { it.isNotBlank() && it != WifiManager.UNKNOWN_SSID && it != "<unknown ssid>" }
    }
}
