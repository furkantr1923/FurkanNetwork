package com.furkan.network.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.furkan.network.utils.AltAgHesaplayici
import com.furkan.network.utils.HashYardimcisi
import com.furkan.network.utils.HataMesaji
import com.furkan.network.utils.SifreUretici
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

// ---------------- Alt ağ ----------------

data class AltAgDurumu(
    val ip: String = "",
    val maske: String = "",
    val cikti: AltAgHesaplayici.Cikti? = null
)

class AltAgViewModel : ViewModel() {
    private val _durum = MutableStateFlow(AltAgDurumu())
    val durum: StateFlow<AltAgDurumu> = _durum.asStateFlow()

    fun ipDegisti(m: String) = _durum.update { it.copy(ip = m) }
    fun maskeDegisti(m: String) = _durum.update { it.copy(maske = m) }

    fun hesapla() = _durum.update { it.copy(cikti = AltAgHesaplayici.hesapla(it.ip, it.maske)) }

    fun ornekYukle() = _durum.update {
        AltAgDurumu(ip = "192.168.1.10/24", cikti = AltAgHesaplayici.hesapla("192.168.1.10/24"))
    }
}

// ---------------- SHA-256 ----------------

data class HashDurumu(
    val metin: String = "",
    val metinHash: String = HashYardimcisi.sha256Hex(""),
    val buyukHarf: Boolean = false,
    val beklenen: String = "",
    val dosyaAdi: String? = null,
    val dosyaBoyutu: Long? = null,
    val dosyaHash: String? = null,
    val dosyaHesaplaniyor: Boolean = false,
    val hata: String? = null
)

class HashViewModel(uygulama: Application) : AndroidViewModel(uygulama) {
    private val _durum = MutableStateFlow(HashDurumu())
    val durum: StateFlow<HashDurumu> = _durum.asStateFlow()
    private var is_: Job? = null

    fun metinDegisti(m: String) = _durum.update { it.copy(metin = m, metinHash = HashYardimcisi.sha256Hex(m)) }
    fun buyukHarfDegisti(b: Boolean) = _durum.update { it.copy(buyukHarf = b) }
    fun beklenenDegisti(m: String) = _durum.update { it.copy(beklenen = m) }

    fun dosyaSecildi(uri: Uri) {
        is_?.cancel()
        _durum.update { it.copy(dosyaHesaplaniyor = true, dosyaHash = null, dosyaAdi = null, dosyaBoyutu = null, hata = null) }
        is_ = viewModelScope.launch {
            try {
                val cozucu = getApplication<Application>().contentResolver
                val (ad, hash, boyut) = withContext(Dispatchers.IO) {
                    val ad = try {
                        cozucu.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                            if (c.moveToFirst()) c.getString(0) else null
                        }
                    } catch (e: Exception) {
                        null
                    }
                    val akis = cozucu.openInputStream(uri) ?: throw FileNotFoundException()
                    val (h, b) = akis.use { girdi ->
                        HashYardimcisi.sha256Akis(girdi) { coroutineContext.ensureActive() }
                    }
                    Triple(ad ?: "Seçilen dosya", h, b)
                }
                _durum.update { it.copy(dosyaAdi = ad, dosyaHash = hash, dosyaBoyutu = boyut, dosyaHesaplaniyor = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _durum.update { it.copy(dosyaHesaplaniyor = false, hata = HataMesaji.cevir(e)) }
            }
        }
    }

    fun dosyaTemizle() {
        is_?.cancel()
        _durum.update { it.copy(dosyaAdi = null, dosyaHash = null, dosyaBoyutu = null, dosyaHesaplaniyor = false, hata = null) }
    }
}

// ---------------- Şifre üretici ----------------

data class SifreDurumu(
    val secenekler: SifreUretici.Secenekler = SifreUretici.Secenekler(),
    val sifre: String = "",
    val entropi: Double = 0.0,
    val hata: String? = null
)

class SifreViewModel : ViewModel() {
    private val _durum = MutableStateFlow(SifreDurumu())
    val durum: StateFlow<SifreDurumu> = _durum.asStateFlow()

    init {
        uret()
    }

    fun secenekDegisti(s: SifreUretici.Secenekler) {
        _durum.update { it.copy(secenekler = s) }
        uret()
    }

    fun uret() {
        val s = _durum.value.secenekler
        _durum.update {
            try {
                it.copy(sifre = SifreUretici.uret(s), entropi = SifreUretici.entropiBit(s), hata = null)
            } catch (e: IllegalArgumentException) {
                it.copy(sifre = "", entropi = 0.0, hata = e.message)
            }
        }
    }
}
