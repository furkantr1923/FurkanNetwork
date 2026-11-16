# 📦 APK Üretme Rehberi

## ⚡ En kolay yol: tek komut (Windows)

1. Zip'i bir klasöre çıkarın (ör. `Masaüstü\FurkanNetwork`).
2. Klasörde **`APK-URET.bat`** dosyasına çift tıklayın.
   *ya da* PowerShell'de:
   ```powershell
   cd "$env:USERPROFILE\Desktop\FurkanNetwork"; powershell -ExecutionPolicy Bypass -File .\apk-uret.ps1
   ```
3. Bitince `APK` klasörü açılır: **`FurkanNetwork-1.0.0.apk`** ✅

### Betik ne yapıyor?
| Adım | Açıklama |
|---|---|
| 1 | JDK 17+ arar: `JAVA_HOME`, Android Studio `jbr`, `C:\Program Files\Android\openjdk` (Visual Studio), `C:\Program Files\Microsoft\jdk-*` |
| 2 | Android SDK arar: `ANDROID_HOME`, `%LOCALAPPDATA%\Android\Sdk` (Android Studio), `C:\Program Files (x86)\Android\android-sdk` (Visual Studio) |
| 3 | Platform 36 (yoksa 35) ve Build-Tools 35+ olduğunu kontrol eder; yoksa `sdkmanager` ile kurmayı dener |
| 4 | `local.properties` dosyasına SDK yolunu yazar |
| 5 | `gradlew.bat assembleRelease` çalıştırır (ilk sefer Gradle + kütüphaneleri indirir: **5–15 dk, internet gerekir**) |
| 6 | APK'yı `APK\` klasörüne kopyalar ve klasörü açar |

Hata olursa `hatalar.txt` Not Defteri'nde açılır; tam günlük `derleme-gunlugu.txt` dosyasındadır.

### Gereken yazılım
Aşağıdakilerden **biri** yeterli:
- **Visual Studio 2022/2026** + "Mobile development with .NET" (MAUI) iş yükü → JDK ve Android SDK birlikte gelir.
- **Android Studio** → JDK (`jbr`) ve SDK birlikte gelir.

## 🧩 Sık karşılaşılan sorunlar

| Mesaj | Çözüm |
|---|---|
| `JDK 17 veya üstü bulunamadı` | Visual Studio Installer › Değiştir › ".NET Multi-platform App UI" ya da [Microsoft OpenJDK 17](https://learn.microsoft.com/java/openjdk/download) kurun |
| `Android SDK Platform 36 (veya 35) ... gerekli` | Visual Studio › **Araçlar › Android › Android SDK Yöneticisi** › *Android 16.0 (API 36)* ve *Build-Tools 35/36* işaretleyin › Uygula. SDK `Program Files` altındaysa PowerShell'i **yönetici olarak** çalıştırın |
| `Could not resolve ...` / `Could not GET` | İnternet bağlantısı veya şirket ağı/proxy engeli. Başka bir ağda tekrar deneyin |
| `SDK location not found` | Betiği proje klasöründen çalıştırın; `local.properties` otomatik yazılır |
| Betik açılıp hemen kapanıyor | `APK-URET.bat` kullanın (sonda `pause` var) |
| `e: file:///.../X.kt:12:5 ...` | Kotlin derleme hatası: `hatalar.txt` içeriğini olduğu gibi paylaşın |

## 🔏 İmza (keystore)

- **Varsayılan:** `keystore.properties` yoksa APK, bilgisayarınızdaki otomatik **hata ayıklama (debug) anahtarıyla** imzalanır. Telefona kurulur; arkadaşlarınızla paylaşmak için yeterlidir.
- **Kalıcı yayın anahtarı** (Google Play veya uzun süreli dağıtım için):
  1. Anahtarı proje klasörünün **dışında** oluşturun:
     ```powershell
     keytool -genkeypair -v -keystore C:\Anahtarlar\furkan-network.jks -alias furkannetwork -keyalg RSA -keysize 2048 -validity 10000
     ```
     (`keytool`, JDK'nın `bin` klasöründedir.)
  2. Proje kök klasöründe `keystore.properties` oluşturun:
     ```properties
     storeFile=C:/Anahtarlar/furkan-network.jks
     storePassword=PAROLANIZ
     keyAlias=furkannetwork
     keyPassword=PAROLANIZ
     ```
  3. `apk-uret.ps1`'i tekrar çalıştırın.

> ⚠️ `keystore.properties`, `.jks` ve `.keystore` dosyaları `.gitignore` ile dışlanmıştır. Parolaları **kimseyle paylaşmayın, GitHub'a yüklemeyin, sohbetlere yazmayın.** Anahtarı kaybederseniz aynı uygulamanın güncellemesini yayınlayamazsınız; yedekleyin.
> Debug anahtarıyla kurulmuş sürümün üstüne yayın anahtarlı sürüm kurulamaz (imza farklı): önce eskisini kaldırın.

## 🧪 Birim testleri
```powershell
.\gradlew.bat testDebugUnitTest
```
Rapor: `app\build\reports\tests\testDebugUnitTest\index.html`

## 📱 Telefona kurma
- APK'yı telefona kopyalayın (USB, Drive, WhatsApp "Belge" olarak) → dosyaya dokunun → **Yükle**.
- "Bilinmeyen kaynak" uyarısında ilgili uygulamaya (Dosyalar/Chrome) izin verin.
- Play Protect uyarısı çıkarsa **Yine de yükle** (uygulama Play Store'da olmadığı için normaldir).
