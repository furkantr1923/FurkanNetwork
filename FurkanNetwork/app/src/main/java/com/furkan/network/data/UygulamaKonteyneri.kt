package com.furkan.network.data

import android.content.Context
import com.furkan.network.models.GecmisKaydi
import com.furkan.network.models.TestTuru
import com.furkan.network.network.AgDurumuOkuyucu
import com.furkan.network.network.DnsServisi
import com.furkan.network.network.GenelIpServisi
import com.furkan.network.network.HizTestiServisi
import com.furkan.network.network.KablosuzBilgiServisi
import com.furkan.network.network.PingServisi
import com.furkan.network.network.SslServisi
import com.furkan.network.network.TracerouteServisi
import java.io.File
import kotlin.random.Random

/** Basit bağımlılık kabı: servisler uygulama boyunca tek örnektir. */
class UygulamaKonteyneri(context: Context) {
    private val uygulama = context.applicationContext

    val ayarlar = AyarlarDeposu(uygulama)
    val gecmis = GecmisDeposu(File(uygulama.filesDir, "gecmis.json"))
    val agDurumu = AgDurumuOkuyucu(uygulama)
    val kablosuz = KablosuzBilgiServisi(uygulama)
    val genelIp = GenelIpServisi()
    val dns = DnsServisi()
    val ping = PingServisi()
    val traceroute = TracerouteServisi()
    val ssl = SslServisi()
    val hizTesti = HizTestiServisi()

    /** Ayarlarda geçmiş kaydı açıksa sonucu geçmişe ekler. */
    suspend fun gecmiseEkle(tur: TestTuru, baslik: String, ozet: String, ayrinti: String) {
        if (!ayarlar.durum.value.gecmisKaydet) return
        gecmis.ekle(
            GecmisKaydi(
                id = System.currentTimeMillis() * 1000 + Random.nextInt(1000),
                tur = tur,
                baslik = baslik,
                ozet = ozet,
                ayrinti = ayrinti,
                zaman = System.currentTimeMillis()
            )
        )
    }
}
