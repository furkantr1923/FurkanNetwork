# 🛡️ Gizlilik Bildirimi — Furkan Network

**Kısaca:** Reklam yok, analiz yok, takip yok, hesap yok, sunucu yok.

## Cihazda saklananlar
| Veri | Yer | Silme |
|---|---|---|
| Test geçmişi (ping/DNS/traceroute/SSL/hız/IP sonuçları) | Uygulamanın özel klasöründe `gecmis.json` (en fazla 300 kayıt) | Geçmiş › Tümünü sil, Ayarlar › Geçmişi temizle, ya da uygulamayı kaldırma |
| Ayarlar (tema, otomatik genel IP, ping sayısı, geçmiş kaydı) | `SharedPreferences` | Uygulamayı kaldırma |

- Android yedeklemesi kapalıdır (`allowBackup=false`); veriler buluta yedeklenmez.
- Ayarlardan "Test sonuçlarını kaydet" kapatılırsa geçmiş hiç tutulmaz.
- Üretilen şifreler ve SHA-256 girdileri **kaydedilmez**.

## Ağ üzerinden giden istekler
Uygulama, yalnızca aşağıdaki durumlarda dışarıya bağlanır:

| Özellik | Hedef | Ne zaman? |
|---|---|---|
| Genel IP | `api.ipify.org`, `api6.ipify.org` (HTTPS) | Panel/IP ekranı açıkken ve "otomatik sorgu" açıksa; kapalıysa yalnızca yenile düğmesiyle |
| DNS sorgusu | Cihazın kullandığı DNS sunucusu | Kullanıcı "Sorgula" deyince |
| Ping / Traceroute | Kullanıcının girdiği hedef | Kullanıcı "Başlat" deyince |
| SSL bilgisi | Kullanıcının girdiği sunucu (yalnızca TLS el sıkışması) | Kullanıcı "İncele" deyince |
| Hız testi | `speed.cloudflare.com` | Kullanıcı testi başlatınca (isteğe bağlı) |

Bu servisler, standart internet trafiğinde olduğu gibi IP adresinizi görür. Furkan Network bu servislere kişisel bilgi (ad, e-posta, cihaz kimliği, konum) **göndermez**.

## İzinler
Bkz. [IZINLER.md](IZINLER.md). Konum izni isteğe bağlıdır ve yalnızca Wi-Fi ağ adını göstermek içindir.

## Üçüncü taraf kütüphaneler
Yalnızca AndroidX / Jetpack Compose / Kotlin kütüphaneleri. Reklam, analiz veya çökme raporlama SDK'sı yoktur.

## İletişim
Sorular için GitHub deposundaki **Issues** sekmesi.
