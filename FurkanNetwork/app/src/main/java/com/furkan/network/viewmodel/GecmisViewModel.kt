package com.furkan.network.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.data.GecmisDeposu
import com.furkan.network.models.GecmisKaydi
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.HataMesaji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class GecmisViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val k = konteyner
    private val _filtre = MutableStateFlow<TestTuru?>(null)
    val filtre: StateFlow<TestTuru?> = _filtre.asStateFlow()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    val kayitlar: StateFlow<List<GecmisKaydi>> = combine(k.gecmis.kayitlar, _filtre) { liste, f ->
        if (f == null) liste else liste.filter { it.tur == f }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val toplamKayit: StateFlow<List<GecmisKaydi>> = k.gecmis.kayitlar

    fun filtreSec(t: TestTuru?) {
        _filtre.value = t
    }

    fun sil(id: Long) {
        viewModelScope.launch { k.gecmis.sil(id) }
    }

    fun temizle() {
        viewModelScope.launch {
            k.gecmis.temizle()
            _mesaj.value = "Tüm geçmiş silindi."
        }
    }

    /** Görünen kayıtları seçilen dosyaya düz metin olarak yazar. */
    fun disaAktar(uri: Uri) {
        val metin = GecmisDeposu.metneDonustur(kayitlar.value)
        viewModelScope.launch {
            _mesaj.value = try {
                withContext(Dispatchers.IO) {
                    val akis = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: throw IOException("Dosya açılamadı")
                    akis.use { it.write(metin.toByteArray(Charsets.UTF_8)) }
                }
                "Geçmiş metin dosyası olarak kaydedildi."
            } catch (e: Exception) {
                "Dışa aktarılamadı: ${HataMesaji.cevir(e)}"
            }
        }
    }

    fun mesajGosterildi() {
        _mesaj.value = null
    }
}
