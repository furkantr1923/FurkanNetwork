package com.furkan.network.data

import android.util.AtomicFile
import com.furkan.network.models.GecmisKaydi
import com.furkan.network.models.TestTuru
import com.furkan.network.utils.Bicim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Test geçmişi: uygulamanın özel klasöründe tek bir JSON dosyası.
 * Hafif tutmak için Room/SQLite kullanılmaz; en fazla [EN_FAZLA] kayıt saklanır.
 * Yazma işlemi AtomicFile ile yapılır (yarım kalan yazma dosyayı bozmaz).
 */
class GecmisDeposu(dosya: File) {

    private val atomik = AtomicFile(dosya)
    private val kilit = Mutex()
    private val _kayitlar = MutableStateFlow<List<GecmisKaydi>>(emptyList())
    val kayitlar: StateFlow<List<GecmisKaydi>> = _kayitlar.asStateFlow()
    private var yuklendi = false

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            kilit.withLock { yukleGerekirse() }
        }
    }

    suspend fun ekle(kayit: GecmisKaydi) = degistir { liste -> (listOf(kayit) + liste).take(EN_FAZLA) }

    suspend fun sil(id: Long) = degistir { liste -> liste.filterNot { it.id == id } }

    suspend fun temizle() = degistir { emptyList() }

    private suspend fun degistir(donustur: (List<GecmisKaydi>) -> List<GecmisKaydi>) {
        withContext(Dispatchers.IO) {
            kilit.withLock {
                yukleGerekirse()
                val yeni = donustur(_kayitlar.value)
                yaz(yeni)
                _kayitlar.value = yeni
            }
        }
    }

    private fun yukleGerekirse() {
        if (yuklendi) return
        yuklendi = true
        _kayitlar.value = try {
            if (!atomik.baseFile.exists()) {
                emptyList()
            } else {
                val dizi = JSONArray(String(atomik.readFully(), Charsets.UTF_8))
                (0 until dizi.length()).mapNotNull { i -> dizi.optJSONObject(i)?.let(::jsondan) }
            }
        } catch (e: Exception) {
            // Bozuk dosya uygulamayı çökertmez; geçmiş boş başlar.
            emptyList()
        }
    }

    private fun yaz(liste: List<GecmisKaydi>) {
        val dizi = JSONArray()
        liste.forEach { dizi.put(jsona(it)) }
        val akis = try {
            atomik.startWrite()
        } catch (e: IOException) {
            return
        }
        try {
            akis.write(dizi.toString().toByteArray(Charsets.UTF_8))
            atomik.finishWrite(akis)
        } catch (e: IOException) {
            atomik.failWrite(akis)
        }
    }

    private fun jsona(k: GecmisKaydi) = JSONObject()
        .put("id", k.id)
        .put("tur", k.tur.kod)
        .put("baslik", k.baslik)
        .put("ozet", k.ozet)
        .put("ayrinti", k.ayrinti)
        .put("zaman", k.zaman)

    private fun jsondan(j: JSONObject): GecmisKaydi? {
        val id = j.optLong("id", -1L)
        if (id < 0) return null
        return GecmisKaydi(
            id = id,
            tur = TestTuru.koddan(j.optString("tur")),
            baslik = j.optString("baslik"),
            ozet = j.optString("ozet"),
            ayrinti = j.optString("ayrinti"),
            zaman = j.optLong("zaman", 0L)
        )
    }

    companion object {
        const val EN_FAZLA = 300

        /** Kayıtları düz metin olarak dışa aktarım biçimine çevirir. */
        fun metneDonustur(kayitlar: List<GecmisKaydi>): String = buildString {
            appendLine("Furkan Network — Test Geçmişi")
            appendLine("Dışa aktarım: ${Bicim.tarih(System.currentTimeMillis())}")
            appendLine("Kayıt sayısı: ${kayitlar.size}")
            appendLine("=".repeat(44))
            kayitlar.forEach { k ->
                appendLine()
                appendLine("[${k.tur.etiket}] ${k.baslik}")
                appendLine("Tarih: ${Bicim.tarih(k.zaman)}")
                appendLine("Özet: ${k.ozet}")
                if (k.ayrinti.isNotBlank()) {
                    appendLine(k.ayrinti.trimEnd())
                }
                appendLine("-".repeat(44))
            }
        }

        fun kayitMetni(k: GecmisKaydi): String = metneDonustur(listOf(k))
    }
}
