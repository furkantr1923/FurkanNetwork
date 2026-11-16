package com.furkan.network.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/** Metni panoya kopyalar. [hassas] ise Android 13+ klavye önizlemesinde gizlenir (şifreler için). */
fun panoyaKopyala(context: Context, metin: String, hassas: Boolean = false) {
    val pano = context.getSystemService(ClipboardManager::class.java) ?: return
    val veri = ClipData.newPlainText("Furkan Network", metin)
    if (hassas && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        veri.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    try {
        pano.setPrimaryClip(veri)
    } catch (e: Exception) {
        Toast.makeText(context, "Kopyalanamadı", Toast.LENGTH_SHORT).show()
        return
    }
    // Android 13+ sistem zaten "kopyalandı" bildirimi gösterir.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Panoya kopyalandı", Toast.LENGTH_SHORT).show()
    }
}

/** Metni Android paylaşım menüsüyle paylaşır. */
fun metinPaylas(context: Context, baslik: String, metin: String) {
    val niyet = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, baslik)
        putExtra(Intent.EXTRA_TEXT, metin)
    }
    try {
        context.startActivity(Intent.createChooser(niyet, "Paylaş"))
    } catch (e: Exception) {
        Toast.makeText(context, "Paylaşım uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
    }
}

fun Context.bulAktivite(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/**
 * Ekran kapandığında (geri gidildiğinde / başka sekmeye geçildiğinde) ağ işlemini durdurur.
 * Ekran döndürmede (yapılandırma değişimi) işlem devam eder.
 */
@Composable
fun EkranKapanincaDurdur(durdur: () -> Unit) {
    val context = LocalContext.current
    val guncelDurdur by rememberUpdatedState(durdur)
    DisposableEffect(Unit) {
        onDispose {
            if (context.bulAktivite()?.isChangingConfigurations != true) guncelDurdur()
        }
    }
}
