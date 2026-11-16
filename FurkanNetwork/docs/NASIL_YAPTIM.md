# 🛠️ Furkan Network'ü Nasıl Yaptım? (ve "biri sorarsa" cevaplar)

## Kısa anlatım (30 saniye)
> "Furkan Network, Kotlin ve Jetpack Compose ile yazdığım hafif bir Android ağ analiz uygulaması. Bağlantı durumunu, yerel ve genel IP'yi, DNS sunucularını gösteriyor; ping, traceroute, DNS sorgusu, SSL sertifika incelemesi ve isteğe bağlı hız testi yapıyor. Alt ağ hesaplayıcı, SHA-256 ve şifre üretici internetsiz çalışıyor. En önemli kuralım **uydurma veri göstermemek**: Android bir şeye izin vermiyorsa uygulama bunu açıkça söylüyor."

## Süreç
1. **Gereksinim:** ChatGPT ile 50 maddelik karar listesi çıkardım (kapsam, tasarım, izinler, gizlilik, hariç tutulan özellikler).
2. **Mimari:** Basit MVVM — ekranlar yalnızca çizer, ViewModel durum tutar, `network/` ve `data/` işi yapar, `utils/` saf Kotlin ve test edilebilir.
3. **Kodlama:** Yapay zekâ asistanı **Claude** ile eş programcı olarak çalıştım: ben kararları ve kuralları verdim, kodu birlikte yazdık, ben inceledim.
4. **Test:** Saf Kotlin mantığı için 24 birim testi yazıldı ve çalıştırıldı (alt ağ, IP doğrulama, ping çıktısı çözümleme, SHA-256 test vektörleri, şifre kuralları).
5. **Derleme:** Tek komutluk PowerShell betiği (`apk-uret.ps1`) ile bilgisayarımdaki Visual Studio'nun JDK ve Android SDK'sını kullanarak APK ürettim.
6. **Yayın:** GitHub deposu + Releases'te APK.

## 📋 Özellik listesi (tam)
**Ağ:** bağlantı durumu ve türü (Wi-Fi/mobil/Ethernet/VPN) · internet doğrulaması · yerel IPv4/IPv6 · genel IPv4/IPv6 · ağ geçidi · DNS sunucuları · Özel DNS (DoT) · DNS A/AAAA sorgusu ve süresi · ping (min/ort/maks, kayıp %, başarılı/başarısız, jitter, grafik, başlat/durdur, sürekli mod) · traceroute (30 atlama, zaman aşımları) · Wi-Fi sinyal (dBm + çubuk), hız, frekans, bant, kanal, SSID (izinle) · mobil veri durumu, SIM, operatör, sinyal seviyesi · SSL (TLS sürümü, şifre takımı, geçerlilik, kalan gün, yayıncı, SAN, SHA-256 parmak izi, zincir) · indirme hızı testi (10/25/50 MB, anlık/ortalama Mbps, ilk bayt süresi).
**Çevrimdışı:** IPv4 alt ağ hesaplayıcı (CIDR/maske, ağ/yayın, host aralığı, sınıf, özel/CGNAT, ikili gösterim, /31-/32) · SHA-256 (metin + dosya + karşılaştırma) · şifre üretici (SecureRandom, entropi).
**Veri:** yerel test geçmişi (filtre, ayrıntı, paylaş, sil, tümünü sil, .txt dışa aktar) · ayarlar (koyu/açık tema, genel IP otomatik, varsayılan ping sayısı, geçmiş kaydı).

## 💬 Sık sorulan sorular — hazır cevaplar

**S: Neden Kotlin + Jetpack Compose?**
C: Android'in resmî ve modern yolu. Compose ile arayüz durumdan otomatik çiziliyor; `StateFlow` ile ViewModel'den gelen veri değişince ekran kendini güncelliyor. Daha az kod, daha az hata.

**S: MVVM'i nasıl uyguladın?**
C: Her ekranın bir ViewModel'i var. ViewModel bir `data class` durumu `StateFlow` olarak yayıyor; ekran `collectAsStateWithLifecycle` ile dinliyor. Ağ işleri `network/` paketindeki servislerde, kayıt işleri `data/` paketinde. Saf hesaplamalar `utils/` paketinde ve Android'e bağımlı olmadığı için birim test edilebiliyor.

**S: Ping'i nasıl yaptın? Android'de ICMP için root gerekmiyor mu?**
C: Ham soket için root gerekir ama Android'in kendi `/system/bin/ping` komutu çoğu cihazda normal uygulamalara da açık. Komutu her paket için `-c 1 -W 2` ile çalıştırıp çıktısındaki `time=` değerini düzenli ifadeyle okuyorum. Bazı cihazlar buna izin vermiyor; o zaman TCP 443 bağlantı süresini ölçüyorum ve ekranda **açıkça** "TCP yöntemi" yazıyor. Sahte ping değeri yok.

**S: Paket kaybını ve jitter'ı nasıl hesaplıyorsun?**
C: Kayıp = yanıt alınamayan paket / gönderilen paket × 100. Jitter = ardışık başarılı yanıtlar arasındaki farkların ortalaması. Bunlar `PingIstatistigi` sınıfında ve birim testli.

**S: Traceroute nasıl çalışıyor?**
C: TTL'i 1'den başlatıp artırıyorum. TTL bitince yoldaki yönlendirici "Time to live exceeded" diye cevap veriyor; o adresi o atlama olarak yazıyorum. Hedef cevap verince bitiyor. Ara atlamaların süresini ping vermediği için ölçtüğüm işlem süresini "≈" ile gösteriyorum — dürüst olmak için. Ağ engelliyorsa rota gösterilmiyor, nedeni yazılıyor.

**S: DNS sorgusunu nasıl yaptın?**
C: Android 10+'da `DnsResolver` API'siyle A ve AAAA kayıtlarını ayrı ayrı sorguluyorum; DNS yanıt kodunu (NXDOMAIN, SERVFAIL…) da okuyup Türkçe açıklıyorum. Eski sürümlerde sistem çözümleyicisine düşüyor ve bunu ekranda belirtiyorum.

**S: Genel IP'yi nereden alıyorsun? Gizlilik?**
C: `api.ipify.org` servisinden HTTPS ile. Gelen yanıtın gerçekten IP adresi olduğunu doğruluyorum. Ayarlardan otomatik sorgu kapatılabiliyor; o zaman yalnızca kullanıcı yenile derse istek gidiyor.

**S: Wi-Fi adı (SSID) neden izin istiyor?**
C: Android, Wi-Fi adını konum bilgisi sayıyor; konum izni olmadan gizliyor. Ben izni açılışta istemiyorum, sadece kullanıcı "ağ adını göster" derse istiyorum. Konum okumuyorum.

**S: SSL bilgisini nasıl alıyorsun?**
C: Hedefe TCP bağlantısı açıp üzerine `SSLSocket` kuruyorum (SNI ile). El sıkışmadan sonra sertifika zincirini, TLS sürümünü, şifre takımını okuyorum; host adı doğrulamasını Android'in varsayılan doğrulayıcısıyla yapıyorum. Güvenilmeyen sertifikaları kabul eden "güvensiz" bir kod yazmadım.

**S: Hız testi nasıl?**
C: Cloudflare'ın genel test adresinden seçilen miktarda veri indirip geçen süreye bölüyorum (bayt × 8 / saniye). 250 ms'de bir anlık hız hesaplayıp grafik çiziyorum, 30 saniye sınırı var, mobil veride önce uyarıyorum.

**S: Geçmişi nerede saklıyorsun, neden Room kullanmadın?**
C: Kayıtlar basit olduğu için tek bir JSON dosyasında (`AtomicFile` ile güvenli yazma, `Mutex` ile eşzamanlılık kontrolü). Room eklemek APK'yı büyütür ve bu ihtiyaç için gereksiz. Ayarlar `SharedPreferences`'ta.

**S: Ekrandan çıkınca ping devam ediyor mu? Pil?**
C: Hayır. İşler `viewModelScope` içinde; ekran kapanınca iptal oluyor, ping süreci de sonlandırılıyor. Başka sekmeye geçince de duruyor. Ağ durumu dinleyicisi uygulama arka plana geçince bırakılıyor.

**S: Uygulama çökmesin diye ne yaptın?**
C: Her ağ işlemi `try/catch` içinde; istisnalar Türkçe açıklamaya çevriliyor (zaman aşımı, bilinmeyen host, bağlantı reddi, SSL hatası…). Zaman aşımları var, girdiler doğrulanıyor (`https://` önekini, yolu, portu temizliyorum; Türkçe karakterli alan adlarını IDN'e çeviriyorum).

**S: Test yazdın mı?**
C: Evet, 24 birim testi: alt ağ hesapları (/24, /20, /31, /32, /0, hatalı girdiler), IPv4/IPv6 doğrulama, ping çıktısı çözümleme, SHA-256 resmi test vektörleri, şifre kuralları (200 tekrar), sinyal ve kanal hesapları.

**S: Bu projeyi yapay zekâ mı yazdı?**
C: Yapay zekâyı (Claude) **eş programcı** olarak kullandım. Gereksinimleri ve 50 kararı ben belirledim, mimariyi birlikte kurduk, kodu inceledim, derlemeyi ve telefonda testi ben yaptım, GitHub'da ben yayınladım. Bugün sektörde de böyle çalışılıyor; önemli olan kodu anlamak ve doğrulamak.

**S: Eksikleri neler?**
C: Yükleme testi, port testi ve ağ taraması bilerek yok. Traceroute yalnızca IPv4. Bazı cihazlar ICMP ping'e izin vermiyor (o zaman TCP ölçüyor). Mobil ağ türü (4G/5G) ek izin istediği için gösterilmiyor.

## 🎤 Sunumda canlı demo önerisi (2 dk)
1. Panel → bağlantı türü, yerel ve genel IP.
2. Ping → `google.com`, 10 paket → grafik ve kayıp %.
3. Uçak modunu aç → Panel "Bağlantı yok" → Araçlar › Alt ağ: `192.168.1.10/24` (internetsiz çalışıyor).
4. Geçmiş → kaydı aç → Paylaş.
