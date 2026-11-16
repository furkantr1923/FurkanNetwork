# 🐙 Furkan Network — GitHub'a Yükleme Rehberi (komut satırı yok)

> Depo adı: **`FurkanNetwork`**. Ayrı bir **git** kurulumu gerekmez: Visual Studio ve Android Studio kendi Git'lerini taşır.

## ✅ 0. Yüklemeden önce güvenlik kontrolü (2 dk)
- [ ] Kök klasörde **`.gitignore`** var (zip'te hazır): `build/`, `.gradle/`, `local.properties`, `*.apk`, `APK/`, `keystore.properties`, `*.jks`, `*.keystore`, `derleme-gunlugu.txt` dışlanır.
- [ ] `keystore.properties` ve `.jks` dosyanız varsa proje klasöründe **olmasın** ya da `.gitignore`'da olduğundan emin olun.
- [ ] Ekran görüntülerinde **genel IP adresiniz ve Wi-Fi adınız** görünmesin (karartın).
- [ ] Kodda parola, telefon numarası, e-posta yok (zip'teki kodda yoktur).

## 1. GitHub hesabı
github.com → giriş yapın. E-postanızı gizli tutmak için: **Settings › Emails › Keep my email addresses private** ✅.

## 2-A. Visual Studio ile (önerilen — zaten kurulu)
1. Visual Studio'yu açın → **Yerel klasör aç** (Open a local folder) → `FurkanNetwork` klasörünü seçin.
2. **Dosya › Hesap Ayarları** → **Ekle › GitHub** → tarayıcıda izin verin (bir kez yapılır).
3. Üst menü **Git › Git Deposu Oluştur…** (Create Git Repository).
4. Solda **GitHub** seçili olsun:
   - **Depo adı:** `FurkanNetwork`
   - **Açıklama:** aşağıdaki "About" metni
   - **Özel (Private):** önce işaretli bırakın; kontrol ettikten sonra herkese açarsınız.
5. **Oluştur ve Gönder** (Create and Push). ✔️

## 2-B. Android Studio ile (alternatif)
1. **File › Open** → `FurkanNetwork` klasörü.
2. **Settings › Version Control › GitHub** → hesabınızı ekleyin.
3. **Git › GitHub › Share Project on GitHub** → Repository name: `FurkanNetwork` → **Share**.
4. Açılan pencerede dosya listesinde `local.properties`, `build`, `APK` **olmadığını** kontrol edin → **Add**.

## 3. GitHub sayfasını düzenleyin
Depo sayfasında sağdaki ⚙️ (About):
- **Description:** `Lightweight Android network diagnostics toolkit — IP, DNS, ping, traceroute, SSL, speed test, subnet calculator. Kotlin · Jetpack Compose. Türkçe.`
- **Topics:** `android, kotlin, jetpack-compose, material3, network-tools, ping, traceroute, dns-lookup, subnet-calculator, ssl-certificate, mvvm, turkish`
- README otomatik görünür. Ekran görüntülerini `docs/ekran-goruntuleri/` klasörüne ekleyin (bkz. oradaki README).

## 4. APK'yı "Release" olarak yayınlayın
1. APK'yı üretin: `APK-URET.bat` (bkz. `docs/APK_URETME.md`) → `APK\FurkanNetwork-1.0.0.apk`.
2. GitHub depo sayfası › **Releases › Create a new release**.
3. **Tag:** `v1.0.0` · **Başlık:** `Furkan Network 1.0.0` · Açıklama: `CHANGELOG.md`'den kopyalayın.
4. APK'yı sürükleyip bırakın → **Publish release**.
> APK'yı kod deposuna değil, yalnızca **Releases**'e ekleyin (`.gitignore` zaten engeller).

## 5. Sonraki değişiklikleri göndermek
**Visual Studio:** Görünüm › **Git Değişiklikleri** → mesaj yazın (ör. `Ping ekranına CSV dışa aktarma eklendi`) → **Tümünü Kaydet** → **Gönder (Push ↑)**.
**Android Studio:** **Git › Commit** (Ctrl+K) → mesaj → **Commit and Push**.

## 6. Depoyu herkese açmak
Settings › General › **Danger Zone › Change visibility › Public** (önce 0. adımı tekrar kontrol edin).

## 7. Profil README'sine ekleme (isteğe bağlı)
```markdown
### 📡 [Furkan Network](https://github.com/furkantr1923/FurkanNetwork) — Android ağ analiz ve tanı araç seti
- 📶 Ping (min/ort/maks, paket kaybı, grafik), traceroute, DNS A/AAAA sorgusu
- 🌐 Yerel/genel IP, ağ geçidi, DNS sunucuları, Wi-Fi/mobil sinyal bilgisi
- 🔐 SSL sertifika incelemesi · ⚡ isteğe bağlı indirme hızı testi
- 🧮 Çevrimdışı: IPv4 alt ağ hesaplayıcı, SHA-256, güvenli şifre üretici
- 🚫 Reklam yok, takip yok, uydurma veri yok

`Kotlin` `Jetpack Compose` `Material 3` `MVVM` `Coroutines` `JUnit`
```

## ❓ Sorun giderme
| Sorun | Çözüm |
|---|---|
| "Git Deposu Oluştur" menüsü yok | Visual Studio Installer › Değiştir › Tek tek bileşenler › **Git for Windows** ✅ |
| Gönderme (push) reddedildi | Hesap Ayarları'ndan GitHub'dan çıkıp tekrar giriş yapın |
| Yanlışlıkla `build/` veya `local.properties` yüklendi | `.gitignore` kök klasörde mi kontrol edin; dosyaları GitHub'dan silip tekrar commit edin |
| `gradlew` Linux/Mac'te çalışmıyor | Dosya izinleri için GitHub'da sorun değil; yerelde `chmod +x gradlew` |
