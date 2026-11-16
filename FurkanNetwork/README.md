# Furkan Network

**A lightweight Android network diagnostics and analysis toolkit.**
Hafif, reklamsız ve takipsiz bir Android ağ analiz ve tanı araç seti.

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)
![Lisans](https://img.shields.io/badge/lisans-MIT-blue)

> Bağımsız kişisel bir projedir. Uygulama yalnızca temel ve yasal bağlantı kontrolleri yapar; port taraması, ağ taraması veya yetkisiz erişim içermez.

---

## 1. Proje hakkında

Furkan Network; bağlantı durumunu, IP ve DNS bilgilerini gösteren, ping / traceroute / DNS sorgusu / SSL sertifika incelemesi ve isteğe bağlı indirme hızı testi yapan bir Android uygulamasıdır. Alt ağ hesaplayıcı, SHA-256 ve şifre üretici **internet olmadan** çalışır.

Temel ilke: **Uydurma veri yok.** Ölçülemeyen bir değer "—", "Okunamadı" veya nedeniyle birlikte gösterilir. Android bir özelliğe izin vermiyorsa (ör. ICMP ping ya da traceroute), bu durum açıkça yazılır; sahte sonuç üretilmez.

## 2. Özellikler

| Ekran | Ne yapar? |
|---|---|
| 🏠 **Panel** | Bağlantı durumu (internet doğrulandı mı), bağlantı türü, VPN/ölçümlü rozetleri, yerel IPv4, genel IP, DNS sunucusu, hızlı erişim düğmeleri |
| 🌐 **IP Bilgisi** | Tüm yerel IPv4/IPv6 adresleri (bağlantı-yerel ayrımıyla), genel IPv4/IPv6, ağ geçidi, arayüz adı, DNS sunucuları, Özel DNS (DoT) durumu, yenile ve geçmişe kaydet |
| 🔎 **DNS Sorgusu** | A ve AAAA kayıtları **ayrı ayrı** (Android 10+ `DnsResolver`), her biri için sorgu süresi, NXDOMAIN/SERVFAIL gibi hata kodlarının Türkçe açıklaması |
| 📶 **Ping** | Başlat/Durdur, 5/10/20/sürekli paket, en az/ortalama/en çok gecikme, paket kaybı %, başarılı/başarısız sayısı, jitter, canlı çizgi grafik |
| 🧭 **Traceroute** | Artan TTL ile en fazla 30 atlama, zaman aşımları "*" ile; Android/ağ izin vermiyorsa açıklama (sahte atlama yok) |
| 📡 **Ağ Bilgisi** | Wi-Fi durumu, sinyal gücü (dBm + 4 çubuk), bağlantı hızı, frekans/bant/kanal, isteğe bağlı SSID; mobil veri durumu, SIM, operatör, sinyal seviyesi; internet/VPN/ölçümlü bilgisi |
| 🔐 **SSL Sertifikası** | TLS sürümü, şifre takımı, ad doğrulaması, geçerlilik ve kalan gün, yayıncı, anahtar türü, SAN listesi, SHA-256 parmak izi, sertifika zinciri |
| ⚡ **İndirme Hızı Testi** | İsteğe bağlı; 10/25/50 MB, anlık + ortalama Mbps, ilk bayt süresi, grafik, 30 sn sınırı, mobil veride uyarı |
| 🧮 **Alt Ağ Hesaplayıcı** *(çevrimdışı)* | CIDR veya maske girişi; ağ/yayın adresi, maske, joker, ilk/son host, host sayısı, sınıf, özel/genel/CGNAT türü, ikili gösterim; /31 ve /32 desteği |
| #️⃣ **SHA-256** *(çevrimdışı)* | Metin ve **dosya** özeti, büyük/küçük harf, beklenen değerle karşılaştırma |
| 🔑 **Şifre Üretici** *(çevrimdışı)* | `SecureRandom`, 6–64 karakter, karakter türleri, benzer karakterleri çıkarma, entropi ve güç etiketi; panoya "hassas" olarak kopyalama |
| 🕘 **Geçmiş** | Tüm test sonuçları yerelde; türe göre filtre, ayrıntı görüntüleme, tek kayıt silme/paylaşma, tümünü silme, **.txt olarak dışa aktarma** |
| ⚙️ **Ayarlar** | Koyu/açık tema, genel IP otomatik sorgu, varsayılan ping sayısı, geçmişi kaydet/temizle, sürüm, gizlilik, hakkında |

Bu sürümde **olmayanlar** (bilinçli olarak): yükleme hızı testi, port testi, ağ taraması.

## 3. Ekran görüntüleri

Ekran görüntüleri `docs/ekran-goruntuleri/` klasöründedir. Uygulamayı kendi cihazınızda çalıştırdıktan sonra çekip buraya ekleyin:

| Panel | Ping | Traceroute | Araçlar |
|---|---|---|---|
| `docs/ekran-goruntuleri/panel.png` | `docs/ekran-goruntuleri/ping.png` | `docs/ekran-goruntuleri/traceroute.png` | `docs/ekran-goruntuleri/araclar.png` |

<!-- Görüntüler eklendiğinde yukarıdaki tabloyu şu biçime çevirin:
| ![Panel](docs/ekran-goruntuleri/panel.png) | ![Ping](docs/ekran-goruntuleri/ping.png) | ... |
-->

## 4. Teknolojiler

| Alan | Kullanılan |
|---|---|
| Dil | Kotlin 2.1 |
| Arayüz | Jetpack Compose, Material 3, Material Icons |
| Mimari | Basit MVVM (`AndroidViewModel` + `StateFlow`), Coroutines / Flow |
| Gezinti | Navigation Compose, alt gezinti çubuğu |
| Ağ | `ConnectivityManager`, `LinkProperties`, `NetworkCallback`, `DnsResolver`, `SSLSocket`, `HttpURLConnection`, sistem `ping` komutu |
| Depolama | Geçmiş için tek JSON dosyası (`AtomicFile` + `org.json`), ayarlar için `SharedPreferences` — Room/SQLite yok |
| Grafik | Compose `Canvas` ile kendi çizgi grafiği — harici kütüphane yok |
| Test | JUnit 4 (alt ağ, IP/host doğrulama, ping çıktısı çözümleyici, SHA-256, şifre üretici, sinyal, X.500, biçimlendirme) |
| Derleme | Gradle 8.11.1, Android Gradle Plugin 8.10.1, JDK 17 |

## 5. Kurulum (telefona)

1. [Releases](../../releases) sayfasından `FurkanNetwork-1.0.0.apk` dosyasını indirin.
2. Telefonda dosyayı açın; istenirse **Bilinmeyen uygulamaları yükle** iznini tarayıcı/dosya yöneticisi için verin.
3. **Yükle** → **Aç**.

Gereksinim: Android 8.0 (API 26) veya üzeri.

## 6. Derleme (APK üretme)

### Yöntem A — Tek komut (Windows, Android Studio gerekmez)
Bilgisayarınızda Visual Studio'nun **.NET MAUI / Android** bileşeni ya da Android Studio kuruluysa JDK ve Android SDK zaten vardır. Proje klasöründe:

```powershell
powershell -ExecutionPolicy Bypass -File .\apk-uret.ps1
```
veya `APK-URET.bat` dosyasına çift tıklayın. Betik JDK'yı ve SDK'yı bulur, `local.properties` yazar, `gradlew assembleRelease` çalıştırır ve APK'yı `APK\FurkanNetwork-1.0.0.apk` olarak kopyalayıp klasörü açar. İlk derleme Gradle ve kütüphaneleri indirdiği için 5–15 dakika sürebilir.

### Yöntem B — Android Studio
1. **File › Open** → proje klasörünü seçin, Gradle eşitlemesini bekleyin.
2. **Build › Generate Signed App Bundle / APK › APK** ya da hızlı deneme için **Build › Build APK(s)**.

### Yöntem C — Komut satırı
```bash
./gradlew assembleRelease      # macOS / Linux
gradlew.bat assembleRelease    # Windows
./gradlew testDebugUnitTest    # birim testleri
```

Ayrıntılar ve yayın imzası (keystore) için: [`docs/APK_URETME.md`](docs/APK_URETME.md)

## 7. Android uyumluluğu

| | |
|---|---|
| En düşük sürüm | Android 8.0 (API 26) |
| Hedef sürüm | Android 16 (API 36) |
| Ekranlar | Telefon ve tablet; kaydırılabilir düzen, kenardan kenara (edge-to-edge) |
| Tema | Koyu (varsayılan) ve açık |
| Sürüme göre farklar | DNS A/AAAA ayrı sorgusu ve hata kodları Android 10+; Özel DNS bilgisi ve mobil sinyal seviyesi Android 9+; daha eski sürümlerde sistem çözümleyicisi kullanılır ve bu ekranda yazar |

## 8. İzinler

| İzin | Neden? | Zorunlu mu? |
|---|---|---|
| `INTERNET` | DNS, ping, SSL, hız testi, genel IP | Evet |
| `ACCESS_NETWORK_STATE` | Bağlantı türü, yerel IP, ağ geçidi, DNS sunucuları, internet doğrulaması | Evet |
| `ACCESS_WIFI_STATE` | Wi-Fi sinyal gücü, bağlantı hızı, frekans | Evet |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | **Yalnızca** Wi-Fi ağ adını (SSID) göstermek için; Android SSID'yi konum izni olmadan gizler. Kullanıcı "Ağ adını göster" demedikçe istenmez. Konum okunmaz. | Hayır (isteğe bağlı) |

Ayrıntı: [`docs/IZINLER.md`](docs/IZINLER.md)

## 9. Gizlilik

- Reklam, analiz (analytics), çökme raporlayıcı veya takip kütüphanesi **yoktur**. Hesap gerekmez; sunucu yoktur.
- Geçmiş ve ayarlar yalnızca cihazda, uygulamanın özel klasöründe tutulur; yedekleme kapalıdır (`allowBackup=false`).
- Dışarıya giden istekler yalnızca kullanıcının başlattığı testlerdir: genel IP için `api.ipify.org` (ayarlardan otomatik sorgu kapatılabilir), hız testi için `speed.cloudflare.com`, diğer testlerde kullanıcının girdiği hedef.
- Şifre üretici ve SHA-256 tamamen çevrimdışıdır; üretilen şifreler kaydedilmez.

Ayrıntı: [`docs/GIZLILIK.md`](docs/GIZLILIK.md)

## 10. Proje yapısı

```
FurkanNetwork/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/furkan/network/
│       │   │   ├── FurkanNetworkApp.kt      # Uygulama sınıfı, servis kabı
│       │   │   ├── MainActivity.kt          # Compose giriş noktası
│       │   │   ├── ui/theme/                # Renkler, tema (koyu lacivert / mavi / beyaz)
│       │   │   ├── ui/navigation/           # Rotalar, alt gezinti, NavHost
│       │   │   ├── screens/                 # 16 ekran (Panel, Ping, DNS, …)
│       │   │   ├── components/              # Kartlar, satırlar, grafik, logo, pano
│       │   │   ├── network/                 # Ağ durumu, DNS, ping, traceroute, SSL, hız testi
│       │   │   ├── data/                    # Geçmiş (JSON), ayarlar (SharedPreferences)
│       │   │   ├── models/                  # Veri sınıfları
│       │   │   ├── utils/                   # Saf Kotlin: alt ağ, hash, şifre, doğrulama, çözümleyici
│       │   │   └── viewmodel/               # Ekran ViewModel'leri
│       │   └── res/                         # Tema, uyarlanabilir simge
│       └── test/                            # JUnit birim testleri
├── docs/                                    # Belgeler ve ekran görüntüleri
├── gradle/ · gradlew · gradlew.bat          # Gradle sarmalayıcı
├── apk-uret.ps1 · APK-URET.bat              # Tek komutla APK (Windows)
└── README.md · CHANGELOG.md · LICENSE
```

Mimari ayrıntısı: [`docs/MIMARI.md`](docs/MIMARI.md) · Test durumu: [`docs/TEST_RAPORU.md`](docs/TEST_RAPORU.md) · Sınırlamalar: [`docs/BILINEN_SINIRLAMALAR.md`](docs/BILINEN_SINIRLAMALAR.md)

## 11. Geliştirici

**Furkan Baykal** — Yazılım Mühendisi
GitHub: [@furkantr1923](https://github.com/furkantr1923)

Bu proje, yapay zekâ asistanı (Claude) eş programcı olarak kullanılarak geliştirilmiştir: gereksinimler, kararlar, derleme, cihaz testi ve yayın geliştiriciye aittir. Ayrıntı: [`docs/NASIL_YAPTIM.md`](docs/NASIL_YAPTIM.md)

Sorular ve öneriler için **Issues** sekmesini kullanabilirsiniz.

## Lisans
[MIT](LICENSE)
