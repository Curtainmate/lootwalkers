param(
    [int]$VersionCode = 34,
    [string]$VersionName = "0.7.14"
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$LocalProperties = Join-Path $Root "local.properties"
$BuildDir = Join-Path $Root "manual-build"
$OutputApk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$SafeVersionName = $VersionName -replace '[^A-Za-z0-9._-]', '-'
$VersionedOutputApk = Join-Path $Root "app\build\outputs\apk\debug\lootwalkers-$SafeVersionName-beta.apk"
$KeystoreDir = Join-Path $Root "keystores"
$BetaKeystore = Join-Path $KeystoreDir "lootwalkers-beta.keystore"
$JavaHome = "C:\Android Studio\jbr"

function Read-SdkDir {
    if (!(Test-Path $LocalProperties)) {
        throw "local.properties was not found. Add sdk.dir=C\:\\Users\\user\\AppData\\Local\\Android\\Sdk"
    }

    $line = Get-Content $LocalProperties | Where-Object { $_ -like "sdk.dir=*" } | Select-Object -First 1
    if (!$line) {
        throw "local.properties does not contain sdk.dir"
    }

    return ($line.Substring("sdk.dir=".Length) -replace "\\:", ":" -replace "\\\\", "\")
}

function Invoke-Checked {
    param(
        [scriptblock]$Command,
        [string]$Name
    )

    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed"
    }
}

$Sdk = Read-SdkDir
$BuildTools = Join-Path $Sdk "build-tools\37.0.0"
$Platform35 = Join-Path $Sdk "platforms\android-35\android.jar"
$Platform36Preview = Join-Path $Sdk "platforms\android-36.1\android.jar"

if (Test-Path $Platform35) {
    $AndroidJar = $Platform35
} elseif (Test-Path $Platform36Preview) {
    $AndroidJar = $Platform36Preview
} else {
    throw "No supported Android platform found. Install Android SDK Platform 35 or 36.1."
}

$env:JAVA_HOME = $JavaHome
$env:PATH = (Join-Path $JavaHome "bin") + ";" + $env:PATH

$Javac = Join-Path $JavaHome "bin\javac.exe"
$Jar = Join-Path $JavaHome "bin\jar.exe"
$Keytool = Join-Path $JavaHome "bin\keytool.exe"
$Aapt2 = Join-Path $BuildTools "aapt2.exe"
$D8 = Join-Path $BuildTools "d8.bat"
$Zipalign = Join-Path $BuildTools "zipalign.exe"
$Apksigner = Join-Path $BuildTools "apksigner.bat"

Remove-Item -LiteralPath $BuildDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $BuildDir "compiled") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $BuildDir "gen") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $BuildDir "classes") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $BuildDir "dex") | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $OutputApk) | Out-Null
New-Item -ItemType Directory -Force -Path $KeystoreDir | Out-Null

if (!(Test-Path $BetaKeystore)) {
    Invoke-Checked {
        & $Keytool -genkeypair `
            -v `
            -keystore $BetaKeystore `
            -storepass lootwalkers-beta `
            -keypass lootwalkers-beta `
            -alias lootwalkers-beta `
            -keyalg RSA `
            -keysize 2048 `
            -validity 10000 `
            -dname "CN=Lootwalkers Beta, OU=Beta, O=Curtainmate, L=Local, S=Local, C=US"
    } "keytool generate beta keystore"
}

$ManifestPath = Join-Path $Root "app\src\main\AndroidManifest.xml"
$BuildManifestPath = Join-Path $BuildDir "AndroidManifest.xml"
$Manifest = Get-Content $ManifestPath -Raw
$Manifest = $Manifest -replace '<manifest xmlns:android="http://schemas.android.com/apk/res/android">', "<manifest xmlns:android=`"http://schemas.android.com/apk/res/android`" package=`"com.curtainmate.lootwalkers`" android:versionCode=`"$VersionCode`" android:versionName=`"$VersionName`">"
$Compatibility = '<uses-sdk android:minSdkVersion="26" android:targetSdkVersion="35" />' + "`r`n    " +
    '<supports-screens android:smallScreens="true" android:normalScreens="true" android:largeScreens="true" android:xlargeScreens="true" android:anyDensity="true" />' + "`r`n    " +
    '<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />'
$Manifest = $Manifest -replace '<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />', $Compatibility
Set-Content -LiteralPath $BuildManifestPath -Value $Manifest -Encoding UTF8

Invoke-Checked { & $Aapt2 compile --dir (Join-Path $Root "app\src\main\res") -o (Join-Path $BuildDir "compiled\res.zip") } "aapt2 compile"
Invoke-Checked {
    & $Aapt2 link -I $AndroidJar `
        --manifest $BuildManifestPath `
        --java (Join-Path $BuildDir "gen") `
        --min-sdk-version 26 `
        --target-sdk-version 35 `
        --version-code $VersionCode `
        --version-name $VersionName `
        --replace-version `
        --no-compile-sdk-metadata `
        -o (Join-Path $BuildDir "unsigned.apk") `
        (Join-Path $BuildDir "compiled\res.zip")
} "aapt2 link"

$JavaFiles = @(Get-ChildItem -Path (Join-Path $Root "app\src\main\java") -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$RFiles = @(Get-ChildItem -Path (Join-Path $BuildDir "gen") -Recurse -Filter *.java | ForEach-Object { $_.FullName })
$Sources = @($JavaFiles + $RFiles)

Invoke-Checked { & $Javac -encoding UTF-8 -source 1.8 -target 1.8 -classpath $AndroidJar -d (Join-Path $BuildDir "classes") $Sources } "javac"
Invoke-Checked { & $Jar cf (Join-Path $BuildDir "classes.jar") -C (Join-Path $BuildDir "classes") . } "jar"
Invoke-Checked { & $D8 --lib $AndroidJar --min-api 26 --output (Join-Path $BuildDir "dex") (Join-Path $BuildDir "classes.jar") } "d8"

$UnsignedWithDex = Join-Path $BuildDir "unsigned-with-dex.apk"
Copy-Item -LiteralPath (Join-Path $BuildDir "unsigned.apk") -Destination $UnsignedWithDex -Force

Add-Type -AssemblyName System.IO.Compression.FileSystem
$Apk = [System.IO.Compression.ZipFile]::Open($UnsignedWithDex, "Update")
try {
    $ExistingDex = $Apk.GetEntry("classes.dex")
    if ($ExistingDex) {
        $ExistingDex.Delete()
    }

    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
        $Apk,
        (Join-Path $BuildDir "dex\classes.dex"),
        "classes.dex"
    ) | Out-Null
} finally {
    $Apk.Dispose()
}

Invoke-Checked { & $Zipalign -f 4 $UnsignedWithDex (Join-Path $BuildDir "aligned.apk") } "zipalign"
Invoke-Checked {
    & $Apksigner sign `
        --ks $BetaKeystore `
        --ks-key-alias lootwalkers-beta `
        --ks-pass pass:lootwalkers-beta `
        --key-pass pass:lootwalkers-beta `
        --out $OutputApk `
        (Join-Path $BuildDir "aligned.apk")
} "apksigner sign"
Invoke-Checked { & $Apksigner verify --print-certs $OutputApk } "apksigner verify"
Copy-Item -LiteralPath $OutputApk -Destination $VersionedOutputApk -Force

& $Aapt2 dump badging $OutputApk | Select-String -Pattern "package:|sdkVersion|targetSdkVersion|supports-screens"
Get-Item $OutputApk | Select-Object FullName, Length, LastWriteTime
Get-Item $VersionedOutputApk | Select-Object FullName, Length, LastWriteTime
