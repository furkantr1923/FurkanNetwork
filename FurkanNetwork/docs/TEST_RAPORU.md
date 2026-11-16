# 🧪 Test Raporu

> Bu rapor **dürüst** tutulmuştur: neyin gerçekten doğrulandığı, neyin henüz cihazda test edilmesi gerektiği ayrı yazılmıştır.

## A. Geliştirme ortamında doğrulananlar

| Kontrol | Sonuç | Not |
|---|---|---|
| `utils` paketi + birim testleri JVM'de derlendi ve çalıştırıldı | ✅ **24/24 test geçti** | Kotlin derleyicisi + JUnit 4 |
| Tüm Kotlin kaynaklarının sözdizimi | ✅ Hata yok | Kotlin derleyicisiyle ayrıştırıldı |
| `network`, `data`, `viewmodel`, `models` tip kontrolü | ✅ Hata yok | Android API'leri için yazılan taklit (stub) sınıflarla derlendi |
| Ekranlar ve bileşenler tip kontrolü | ✅ Hata yok | Compose/Material 3 API'leri için taklit sınıflarla |
| **Gerçek Android SDK ile Gradle derlemesi (APK)** | ⏳ **Yapılmadı** | Geliştirme ortamında Android SDK ve Maven erişimi yoktu |
| **Gerçek cihazda çalıştırma** | ⏳ **Yapılmadı** | Aşağıdaki kontrol listesiyle yapılmalı |

Birim testlerinin kapsadığı alanlar:
- Alt ağ: /24, noktalı maske /20, /31, /32, /0, hatalı girdiler, özel/CGNAT/loopback/APIPA türleri
- IPv4/IPv6 doğrulama, host temizleme (şema, yol, port, `[IPv6]:port`, IDN), hatalı host listesi
- Ping çıktısı: başarılı yanıt, adlı/IPv6 yanıt, `time<1`, zaman aşımı, TTL aşımı (3 biçim), ulaşılamaz, izin hatası; istatistik (kayıp %, jitter)
- SHA-256 bilinen vektörler (`abc`, boş metin), akış özeti, karşılaştırma
- Şifre üretici: 200 tekrarda kural doğrulaması, tür seçilmediğinde hata, entropi
- Wi-Fi sinyal seviyesi, bant ve kanal; X.500 ad ayrıştırma; Türkçe sayı biçimleri

## B. Cihazda yapılacak kontrol listesi (APK üretildikten sonra)

| # | Test | Beklenen | Sonuç |
|---|---|---|---|
| 1 | Uygulama açılışı | Panel açılır, çökme yok | ☐ |
| 2 | Alt gezinti (Panel/Araçlar/Geçmiş/Ayarlar) ve geri düğmesi | Sorunsuz geçiş | ☐ |
| 3 | Wi-Fi'da Panel | "İnternet erişimi var", Wi-Fi, yerel IPv4, genel IP | ☐ |
| 4 | Uçak modu | "Bağlantı yok", genel IP "Bağlantı yok", çökme yok | ☐ |
| 5 | IP Bilgisi › Yenile / Geçmişe kaydet | Değerler güncellenir, kayıt Geçmiş'te | ☐ |
| 6 | DNS: `google.com` | A ve AAAA adresleri + süreler | ☐ |
| 7 | DNS: `olmayan-alan-adi-12345.com` | "Alan adı bulunamadı (NXDOMAIN)" | ☐ |
| 8 | DNS: `8.8.8.8` | "DNS sorgusu için alan adı girin" uyarısı | ☐ |
| 9 | Ping: `google.com`, 10 paket | ms değerleri, %0 kayıp, grafik | ☐ |
| 10 | Ping: `10.255.255.1` | Zaman aşımları, %100 kayıp | ☐ |
| 11 | Ping sürekli → Durdur → geri | Durur, geçmişe kaydedilir | ☐ |
| 12 | Ping sırasında başka sekmeye geçiş | Ping durur | ☐ |
| 13 | Traceroute: `google.com` | Atlamalar veya cihaz desteklemiyorsa açıklama | ☐ |
| 14 | Ağ Bilgisi › "Ağ adını göster" | İzin penceresi; izin verilince SSID | ☐ |
| 15 | SSL: `google.com` | TLS 1.3, geçerli, ad uyumlu | ☐ |
| 16 | SSL: `expired.badssl.com` | Sertifika hatası açıklaması, çökme yok | ☐ |
| 17 | Hız testi 10 MB (Wi-Fi) / mobil veride uyarı | Mbps sonucu / uyarı penceresi | ☐ |
| 18 | Uçak modunda Alt ağ, SHA-256, Şifre | Hepsi çalışır | ☐ |
| 19 | SHA-256 dosya seçimi | Özet ve boyut | ☐ |
| 20 | Geçmiş: filtre, ayrıntı, paylaş, sil, .txt dışa aktar, tümünü sil | Hepsi çalışır | ☐ |
| 21 | Ayarlar: açık tema / koyu tema | Anında değişir | ☐ |
| 22 | Ekranı döndürme (ping çalışırken) | Ping devam eder, çökme yok | ☐ |

Testleri yaptıktan sonra bu tabloyu doldurup depoya ekleyin.
