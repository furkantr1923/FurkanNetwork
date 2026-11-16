# ⚠️ Bilinen Sınırlamalar

| Konu | Açıklama |
|---|---|
| ICMP ping | Bazı cihaz/ROM'lar uygulamaların sistem `ping` komutunu kullanmasına izin vermez. Bu durumda TCP 443 bağlantı kurulum süresi ölçülür ve ekranda açıkça belirtilir. Değerler ICMP ping ile birebir aynı değildir. |
| Traceroute | Yalnızca IPv4. Ara yönlendiricilerin süresi ping tarafından raporlanmadığından "≈" ile gösterilen değer işlem başlatma süresini de içerir. Ağ veya operatör TTL-aşımı (ICMP) yanıtlarını engelliyorsa rota gösterilemez; sahte atlama üretilmez. 5 ardışık yanıtsız atlamada keşif durur. |
| DNS (Android 8–9) | A/AAAA ayrı sorgulanamaz; sistem çözümleyicisi kullanılır ve IPv6 bağlantısı yoksa AAAA sonuçları gizlenebilir. Ekranda yazılır. |
| Wi-Fi ağ adı | Konum izni + açık konum servisi gerekir (Android kuralı). |
| Mobil ağ türü (4G/5G) | `READ_PHONE_STATE` izni gerektirdiği için gösterilmez. |
| Mobil sinyal seviyesi | Android 9+; bazı cihazlarda okunamayabilir. |
| Hız testi | Tek HTTPS bağlantısı, en fazla 30 sn; çok hızlı hatlarda gerçek kapasitenin altında kalabilir. Yükleme testi yoktur. Cloudflare uç noktası değişirse test çalışmayabilir. |
| SSL | Güvenilmeyen/süresi dolmuş sertifikaların ayrıntıları gösterilmez (güvenlik gereği varsayılan güven deposu kullanılır); yalnızca hata açıklanır. |
| Genel IP | `api.ipify.org` servisine bağlıdır; servis erişilemezse "Alınamadı" görünür. |
| Port testi, ağ taraması | Bu sürüme bilinçli olarak eklenmedi. |
| Dil | Yalnızca Türkçe. |

## Yol haritası (fikir)
- Yükleme hızı testi ve çoklu bağlantılı ölçüm
- Ana ekran widget'ı (bağlantı durumu)
- Ping sonuçlarını CSV olarak dışa aktarma
- İngilizce dil desteği
