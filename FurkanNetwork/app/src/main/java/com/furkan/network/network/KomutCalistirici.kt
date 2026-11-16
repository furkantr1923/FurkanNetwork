package com.furkan.network.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

data class KomutSonucu(val cikisKodu: Int, val cikti: String, val zamanAsimi: Boolean)

/**
 * Sistem komutunu (ör. /system/bin/ping) güvenle çalıştırır.
 * Coroutine iptal edilirse süreç sonlandırılır; zaman aşımında da öldürülür.
 */
object KomutCalistirici {

    /** Komut başlatılamazsa (ikili dosya yok / izin yok) null döner. */
    suspend fun calistir(komut: List<String>, zamanAsimiMs: Long): KomutSonucu? = withContext(Dispatchers.IO) {
        val surec = try {
            ProcessBuilder(komut).redirectErrorStream(true).start()
        } catch (e: IOException) {
            return@withContext null
        } catch (e: SecurityException) {
            return@withContext null
        }
        try {
            val bitti = runInterruptible { surec.waitFor(zamanAsimiMs, TimeUnit.MILLISECONDS) }
            if (!bitti) {
                KomutSonucu(-1, "", zamanAsimi = true)
            } else {
                val cikti = try {
                    surec.inputStream.bufferedReader().use { it.readText() }
                } catch (e: IOException) {
                    ""
                }
                KomutSonucu(surec.exitValue(), cikti, zamanAsimi = false)
            }
        } finally {
            surec.destroy()
        }
    }
}
