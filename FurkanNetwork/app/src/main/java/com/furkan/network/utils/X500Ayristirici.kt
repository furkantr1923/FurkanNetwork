package com.furkan.network.utils

/** RFC 2253 biçimindeki sertifika adlarından alan okur (ör. CN, O). */
object X500Ayristirici {

    /** "CN=*.google.com,O=Google LLC,C=US" → {CN=*.google.com, O=Google LLC, C=US} */
    fun ayristir(ad: String): Map<String, String> {
        val sonuc = LinkedHashMap<String, String>()
        val parca = StringBuilder()
        val parcalar = mutableListOf<String>()
        var kacis = false
        var tirnak = false
        for (c in ad) {
            when {
                kacis -> { parca.append(c); kacis = false }
                c == '\\' -> { parca.append(c); kacis = true }
                c == '"' -> { parca.append(c); tirnak = !tirnak }
                (c == ',' || c == '+') && !tirnak -> { parcalar += parca.toString(); parca.clear() }
                else -> parca.append(c)
            }
        }
        if (parca.isNotEmpty()) parcalar += parca.toString()
        for (p in parcalar) {
            val i = p.indexOf('=')
            if (i <= 0) continue
            val anahtar = p.substring(0, i).trim().uppercase()
            val deger = p.substring(i + 1).trim().removeSurrounding("\"").replace("\\", "")
            if (anahtar !in sonuc) sonuc[anahtar] = deger
        }
        return sonuc
    }

    fun alan(ad: String, anahtar: String): String? = ayristir(ad)[anahtar.uppercase()]

    /** Okunabilir kısa ad: önce CN, yoksa O, yoksa ham metin. */
    fun kisaAd(ad: String): String {
        val a = ayristir(ad)
        return a["CN"] ?: a["O"] ?: ad
    }
}
