# ============================================================
#  Furkan Network - tek komutla APK üretme betiği (Windows)
#  Ne yapar?
#   1) Bilgisayardaki JDK 17+ kurulumunu bulur (Visual Studio / Android Studio / Microsoft JDK)
#   2) Android SDK'yı bulur (Visual Studio'nun ya da Android Studio'nun SDK'sı)
#   3) local.properties dosyasını yazar (SDK yolu)
#   4) gradlew.bat assembleRelease ile imzalı APK üretir
#   5) APK'yı APK\FurkanNetwork-<sürüm>.apk olarak kopyalar ve klasörü açar
#  İlk çalıştırmada Gradle ve kütüphaneler indirilir (internet gerekir, 5-15 dk).
# ============================================================
# Not: Gradle uyarıları stderr'e yazar; 'Stop' kullanılırsa PowerShell 5.1 derlemeyi yarıda keser.
$ErrorActionPreference = 'Continue'
try { [Console]::OutputEncoding = [Text.Encoding]::UTF8 } catch { }
Set-Location -LiteralPath $PSScriptRoot

function Yaz($metin, $renk = 'Cyan') { Write-Host $metin -ForegroundColor $renk }
function Dur($metin) {
    Write-Host ""
    Write-Host "HATA: $metin" -ForegroundColor Red
    Write-Host ""
    exit 1
}

Yaz "=== Furkan Network APK üretimi ===" 'White'

# ---------- 1) JDK ----------
function JdkSurumu($yol) {
    $rel = Join-Path $yol 'release'
    if (-not (Test-Path (Join-Path $yol 'bin\java.exe'))) { return 0 }
    if (Test-Path $rel) {
        $satir = Select-String -Path $rel -Pattern '^JAVA_VERSION="?(\d+)' | Select-Object -First 1
        if ($satir) { return [int]$satir.Matches[0].Groups[1].Value }
    }
    return 0
}

$adaylar = @()
if ($env:JAVA_HOME) { $adaylar += $env:JAVA_HOME }
$adaylar += "C:\Program Files\Android\Android Studio\jbr"
$adaylar += "$env:LOCALAPPDATA\Programs\Android Studio\jbr"
foreach ($kok in @("C:\Program Files\Android\openjdk", "C:\Program Files\Microsoft", "C:\Program Files\Eclipse Adoptium", "C:\Program Files\Java")) {
    if (Test-Path $kok) {
        $adaylar += Get-ChildItem -LiteralPath $kok -Directory -Filter 'jdk*' -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | ForEach-Object { $_.FullName }
    }
}
$jdk = $null
foreach ($a in $adaylar) {
    if ($a -and (Test-Path $a) -and ((JdkSurumu $a) -ge 17)) { $jdk = $a; break }
}
if (-not $jdk) {
    Dur "JDK 17 veya üstü bulunamadı. Visual Studio Installer > 'Mobile development with .NET' ya da Android Studio kurulumu JDK içerir. Alternatif: https://learn.microsoft.com/java/openjdk/download (JDK 17)."
}
Yaz "JDK      : $jdk (Java $(JdkSurumu $jdk))"
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;$env:Path"

# ---------- 2) Android SDK ----------
$sdkAdaylari = @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT,
    "$env:LOCALAPPDATA\Android\Sdk",
    "C:\Program Files (x86)\Android\android-sdk",
    "C:\Program Files\Android\android-sdk") | Where-Object { $_ }
$sdk = $null
foreach ($s in $sdkAdaylari) {
    if ((Test-Path (Join-Path $s 'platforms')) -and (Test-Path (Join-Path $s 'build-tools'))) { $sdk = $s; break }
}
if (-not $sdk) {
    Dur "Android SDK bulunamadı. Visual Studio > Araçlar > Android > Android SDK Yöneticisi ile ya da Android Studio ile SDK kurun."
}
Yaz "SDK      : $sdk"

function SurumNo($ad) { try { return [version]($ad -replace '[^\d\.].*$', '') } catch { return [version]'0.0' } }

# Platform (compileSdk): 36 tercih, yoksa 35
$platformlar = Get-ChildItem -LiteralPath (Join-Path $sdk 'platforms') -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match '^android-(\d+)$' } | ForEach-Object { [int]($_.Name -replace 'android-', '') }
$derlemeSdk = $null
if ($platformlar -contains 36) { $derlemeSdk = 36 } elseif ($platformlar -contains 35) { $derlemeSdk = 35 }

# build-tools: 35.0.0 ve üstü
$bt = Get-ChildItem -LiteralPath (Join-Path $sdk 'build-tools') -Directory -ErrorAction SilentlyContinue |
    Where-Object { (SurumNo $_.Name) -ge [version]'35.0.0' -and (Test-Path (Join-Path $_.FullName 'aapt2.exe')) } |
    Sort-Object { SurumNo $_.Name } -Descending | Select-Object -First 1

if (-not $derlemeSdk -or -not $bt) {
    Yaz "Eksik SDK bileşeni var; sdkmanager ile kurmayı deniyorum..." 'Yellow'
    $sm = Get-ChildItem -LiteralPath (Join-Path $sdk 'cmdline-tools') -Recurse -Filter 'sdkmanager.bat' -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending | Select-Object -First 1
    if ($sm) {
        $paketler = @()
        if (-not $derlemeSdk) { $paketler += 'platforms;android-36' }
        if (-not $bt) { $paketler += 'build-tools;35.0.0' }
        $evet = ("y`n" * 20)
        $evet | & $sm.FullName "--sdk_root=$sdk" --licenses | Out-Null
        $evet | & $sm.FullName "--sdk_root=$sdk" @paketler
        if (-not $derlemeSdk -and (Test-Path (Join-Path $sdk 'platforms\android-36'))) { $derlemeSdk = 36 }
        $bt = Get-ChildItem -LiteralPath (Join-Path $sdk 'build-tools') -Directory -ErrorAction SilentlyContinue |
            Where-Object { (SurumNo $_.Name) -ge [version]'35.0.0' } | Sort-Object { SurumNo $_.Name } -Descending | Select-Object -First 1
    }
    if (-not $derlemeSdk -or -not $bt) {
        Dur "Android SDK Platform 36 (veya 35) ve Build-Tools 35+ gerekli. Visual Studio > Araçlar > Android > Android SDK Yöneticisi'nden 'Android 16 (API 36)' platformunu ve 'Build-Tools 35/36' paketini kurun. (SDK 'Program Files' altındaysa PowerShell'i yönetici olarak açmanız gerekebilir.)"
    }
}
Yaz "Platform : android-$derlemeSdk"
Yaz "BuildTool: $($bt.Name)"

# ---------- 3) local.properties ----------
$sdkKacisli = ($sdk -replace '\\', '\\') -replace ':', '\:'
[IO.File]::WriteAllText((Join-Path $PSScriptRoot 'local.properties'), "sdk.dir=$sdkKacisli`r`n")

# ---------- 4) Derleme ----------
Yaz ""
Yaz "Derleme başlıyor (ilk seferde 5-15 dk sürebilir, ekran sessiz kalabilir)..." 'Yellow'
$gunluk = Join-Path $PSScriptRoot 'derleme-gunlugu.txt'
& .\gradlew.bat assembleRelease --console=plain "-PfurkanCompileSdk=$derlemeSdk" "-PfurkanBuildTools=$($bt.Name)" 2>&1 |
    Tee-Object -FilePath $gunluk | ForEach-Object {
        $s = "$_"
        if ($s -match '^(e: |FAILURE|BUILD |\* What went wrong)') { Write-Host $s -ForegroundColor Yellow }
        elseif ($s -match '^> Task ') { Write-Host $s -ForegroundColor DarkGray }
    }
$kod = $LASTEXITCODE

$apk = Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'app\build\outputs\apk\release') -Filter '*.apk' -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($kod -ne 0 -or -not $apk) {
    $hatalar = Join-Path $PSScriptRoot 'hatalar.txt'
    Get-Content $gunluk | Where-Object { $_ -match '^e: |error:|What went wrong|FAILED|Could not|Exception|^\s+> ' } |
        Select-Object -Unique | Set-Content -Path $hatalar -Encoding UTF8
    Write-Host ""
    Write-Host "Derleme BAŞARISIZ. Hata satırları hatalar.txt dosyasına yazıldı (tam günlük: derleme-gunlugu.txt)." -ForegroundColor Red
    Start-Process notepad.exe $hatalar
    exit 1
}

# ---------- 5) APK'yı kopyala ----------
$surum = (Select-String -Path (Join-Path $PSScriptRoot 'app\build.gradle.kts') -Pattern 'versionName = "([^"]+)"').Matches[0].Groups[1].Value
$hedefKlasor = Join-Path $PSScriptRoot 'APK'
New-Item -ItemType Directory -Force -Path $hedefKlasor | Out-Null
$hedef = Join-Path $hedefKlasor "FurkanNetwork-$surum.apk"
Copy-Item -LiteralPath $apk.FullName -Destination $hedef -Force
Write-Host ""
Write-Host "BAŞARILI! APK hazır: $hedef" -ForegroundColor Green
Write-Host ("Boyut: {0:N1} MB" -f ((Get-Item $hedef).Length / 1MB)) -ForegroundColor Green
if (-not (Test-Path (Join-Path $PSScriptRoot 'keystore.properties'))) {
    Write-Host "Not: keystore.properties olmadığı için APK hata ayıklama (debug) anahtarıyla imzalandı; telefona kurulur. Play Store için docs\APK_URETME.md'ye bakın." -ForegroundColor DarkYellow
}
Start-Process explorer.exe $hedefKlasor
