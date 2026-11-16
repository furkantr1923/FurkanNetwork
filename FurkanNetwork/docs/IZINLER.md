# 🔐 İzinler

Furkan Network yalnızca gerekli izinleri ister.

| İzin | Tür | Kullanım yeri | İstenmezse |
|---|---|---|---|
| `INTERNET` | Normal (kurulumda) | DNS, ping, traceroute, SSL, hız testi, genel IP | Ağ testleri çalışmaz |
| `ACCESS_NETWORK_STATE` | Normal | Bağlantı türü, IP adresleri, ağ geçidi, DNS sunucuları, internet doğrulaması, VPN/ölçümlü bilgisi | — |
| `ACCESS_WIFI_STATE` | Normal | Wi-Fi açık mı, sinyal gücü (RSSI), bağlantı hızı, frekans | — |
| `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` | **Çalışma zamanında, isteğe bağlı** | Yalnızca Wi-Fi ağ adı (SSID) | SSID "Gizli (izin gerekli)" görünür, diğer her şey çalışır |

## Konum izni neden var?
Android 8.1'den itibaren Wi-Fi ağ adı (SSID) **konum bilgisi** sayılır; uygulamalar konum izni olmadan SSID'yi okuyamaz. Furkan Network:
- izni **açılışta istemez**; yalnızca Ağ Bilgisi ekranında kullanıcı **"Ağ adını göster"** düğmesine basınca ister,
- konum (GPS / enlem-boylam) **okumaz, saklamaz, göndermez**,
- izin verilse bile cihazın konum servisi kapalıysa Android SSID'yi yine gizler; uygulama bunu ekranda açıklar.

## Bilinçli olarak istenmeyen izinler
| İzin | Neden istenmiyor? |
|---|---|
| `READ_PHONE_STATE` | Mobil ağ türü (4G/5G) için gerekir; gizlilik açısından gereksiz bulundu. Bu bilgi gösterilmez. |
| `ACCESS_BACKGROUND_LOCATION` | Arka planda hiçbir şey yapılmaz. |
| `POST_NOTIFICATIONS` | Bildirim yok. |
| Depolama izinleri | Dosya seçimi/kaydetme Android'in sistem penceresiyle (SAF) yapılır; izin gerekmez. |
