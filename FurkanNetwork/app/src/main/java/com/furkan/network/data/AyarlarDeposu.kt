package com.furkan.network.data

import android.content.Context
import android.content.SharedPreferences
import com.furkan.network.models.Ayarlar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Basit ayarlar: SharedPreferences + StateFlow (veritabanı gerekmez). */
class AyarlarDeposu(context: Context) {

    private val tercihler: SharedPreferences =
        context.getSharedPreferences("furkan_network_ayarlar", Context.MODE_PRIVATE)

    private val _durum = MutableStateFlow(oku())
    val durum: StateFlow<Ayarlar> = _durum.asStateFlow()

    private fun oku() = Ayarlar(
        koyuTema = tercihler.getBoolean(K_KOYU, true),
        genelIpOtomatik = tercihler.getBoolean(K_GENEL_IP, true),
        gecmisKaydet = tercihler.getBoolean(K_GECMIS, true),
        pingAdet = tercihler.getInt(K_PING_ADET, 10)
    )

    fun koyuTemaAyarla(deger: Boolean) = guncelle { putBoolean(K_KOYU, deger) }
    fun genelIpOtomatikAyarla(deger: Boolean) = guncelle { putBoolean(K_GENEL_IP, deger) }
    fun gecmisKaydetAyarla(deger: Boolean) = guncelle { putBoolean(K_GECMIS, deger) }
    fun pingAdetAyarla(deger: Int) = guncelle { putInt(K_PING_ADET, deger) }

    private fun guncelle(blok: SharedPreferences.Editor.() -> Unit) {
        val duzenleyici = tercihler.edit()
        duzenleyici.blok()
        duzenleyici.apply()
        _durum.value = oku()
    }

    private companion object {
        const val K_KOYU = "koyu_tema"
        const val K_GENEL_IP = "genel_ip_otomatik"
        const val K_GECMIS = "gecmis_kaydet"
        const val K_PING_ADET = "ping_adet"
    }
}
