# 🏗️ Mimari

## Katmanlar (basit MVVM)

```
Ekran (Compose)  ──olay──▶  ViewModel  ──çağrı──▶  Servis (network/) / Depo (data/)
     ▲                         │                          │
     └──── StateFlow (durum) ◀─┘                          └── Android API'leri / sistem ping / HTTPS
```

| Paket | Sorumluluk |
|---|---|
| `ui/theme` | Renk paleti (koyu lacivert / mavi / beyaz), açık tema, tipografi, köşe yuvarlaklıkları, durum renkleri (başarı/uyarı/hata) |
| `ui/navigation` | `Rota` listesi, alt gezinti (Panel · Araçlar · Geçmiş · Ayarlar), `NavHost`, üst çubuk ve geri düğmesi |
| `screens` | 16 ekran; yalnızca durumu çizer, iş mantığı içermez |
| `components` | Ortak kartlar, satırlar, düğmeler, giriş alanı, sinyal çubukları, çizgi grafik, logo, pano/paylaş yardımcıları, `EkranKapanincaDurdur` |
| `viewmodel` | Ekran durumu (`data class` + `MutableStateFlow`), işi başlatma/durdurma, geçmişe kaydetme |
| `network` | `AgDurumuOkuyucu`, `GenelIpServisi`, `DnsServisi`, `PingServisi`, `TracerouteServisi`, `KablosuzBilgiServisi`, `SslServisi`, `HizTestiServisi`, `KomutCalistirici` |
| `data` | `GecmisDeposu` (JSON + AtomicFile + Mutex), `AyarlarDeposu` (SharedPreferences), `UygulamaKonteyneri` (basit bağımlılık kabı) |
| `models` | Veri sınıfları (`AgDurumu`, `DnsSonucu`, `PingOrnegi`, `GecmisKaydi` …) |
| `utils` | **Saf Kotlin** (Android'e bağımlı değil, birim testli): alt ağ, IP/host doğrulama, ping çıktısı çözümleyici, istatistik, SHA-256, şifre, sinyal, X.500, biçimlendirme, hata mesajları |

## Önemli tasarım kararları

### 1. Uydurma veri yok
- Genel IP yanıtı gerçekten bir IP değilse gösterilmez.
- Ping'de ICMP kullanılamazsa TCP 443 bağlantı süresi ölçülür ve ekranda **"Yöntem: TCP 443 bağlantı süresi"** + açıklama gösterilir.
- Traceroute'ta ara atlama süresi ping tarafından raporlanmaz; ölçülen işlem süresi "≈" ile işaretlenir. Hiç yanıt yoksa sahte atlama yerine nedeni yazılır.

### 2. Yaşam döngüsü ve pil
- Uzun işler (`ping`, traceroute, hız testi, DNS, SSL) `viewModelScope` içinde çalışır; ekran kapanınca ViewModel temizlenir ve iş iptal olur.
- Alt sekmeye geçişte ekran durumu saklansa da `EkranKapanincaDurdur` işi durdurur (ekran döndürmede durdurmaz).
- Ağ durumu `callbackFlow` + `registerDefaultNetworkCallback` ile dinlenir; `collectAsStateWithLifecycle` + `WhileSubscribed` sayesinde uygulama arka plandayken dinleme durur.
- Ağ Bilgisi ekranı yalnızca görünürken 3 saniyede bir yenilenir.
- Sistem `ping` süreci iptal edildiğinde `Process.destroy()` ile sonlandırılır; her komut zaman aşımına sahiptir.

### 3. Hata yönetimi
- Her ağ işlemi `try/catch` ile sarılır; `HataMesaji.cevir()` istisnaları Türkçe açıklamaya çevirir (zaman aşımı, bilinmeyen host, bağlantı reddi, SSL hatası, izin yok …).
- `CancellationException` yutulmaz (iptal düzgün çalışsın diye) ve zaman aşımları `withTimeoutOrNull` ile ele alınır.
- Bozuk geçmiş dosyası uygulamayı çökertmez; geçmiş boş başlar.

### 4. Hafiflik
- Room, Retrofit, OkHttp, grafik kütüphanesi **yok**. HTTP için `HttpURLConnection`, grafik için Compose `Canvas`.
- Yayın derlemesinde R8 küçültme + kaynak küçültme açık.

## Veri akışı örneği: Ping
1. `PingEkrani` → `vm.baslat()`
2. `HostDogrulayici.ayikla()` girdiyi temizler (`https://`, yol, port, IDN).
3. `PingServisi.calistir()` bir `Flow<PingOlayi>` döndürür: `Hedef` → (gerekirse `YontemDegisti`) → `Ornek` × N.
4. Her örnekte `PingIstatistigi.hesapla()` min/ort/maks/kayıp/jitter üretir; ekran `StateFlow` ile güncellenir.
5. İş bittiğinde veya durdurulduğunda özet `GecmisDeposu`'na yazılır (`NonCancellable`).
