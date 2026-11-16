package com.furkan.network.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.models.Ayarlar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AyarlarViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    val ayarlar: StateFlow<Ayarlar> = k.ayarlar.durum
    val kayitSayisi: StateFlow<Int> = k.gecmis.kayitlar.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), k.gecmis.kayitlar.value.size)

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    fun koyuTema(b: Boolean) = k.ayarlar.koyuTemaAyarla(b)
    fun genelIpOtomatik(b: Boolean) = k.ayarlar.genelIpOtomatikAyarla(b)
    fun gecmisKaydet(b: Boolean) = k.ayarlar.gecmisKaydetAyarla(b)
    fun pingAdet(n: Int) = k.ayarlar.pingAdetAyarla(n)

    fun gecmisiTemizle() {
        viewModelScope.launch {
            k.gecmis.temizle()
            _mesaj.value = "Geçmiş temizlendi."
        }
    }

    fun mesajGosterildi() {
        _mesaj.value = null
    }
}
