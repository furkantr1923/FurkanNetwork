@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.furkan.network.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.furkan.network.ui.theme.LocalDurumRenkleri

/** Ekran içeriği için ortak kaydırılabilir düzen boşlukları. */
val EkranDolgusu = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

@Composable
fun BilgiKarti(
    modifier: Modifier = Modifier,
    baslik: String? = null,
    simge: ImageVector? = null,
    eylem: (@Composable RowScope.() -> Unit)? = null,
    icerik: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (baslik != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (simge != null) {
                        Icon(
                            simge, contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        baslik,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    eylem?.invoke(this)
                }
            }
            icerik()
        }
    }
}

@Composable
fun BilgiSatiri(
    etiket: String,
    deger: String,
    modifier: Modifier = Modifier,
    esGenislik: Boolean = false,
    kopyalanabilir: Boolean = false,
    degerRengi: Color = Color.Unspecified
) {
    val context = LocalContext.current
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            etiket,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            deger,
            style = if (esGenislik) {
                MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = degerRengi,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
        if (kopyalanabilir && deger.isNotBlank() && deger != "—") {
            IconButton(onClick = { panoyaKopyala(context, deger) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Kopyala", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HataKutusu(mesaj: String, modifier: Modifier = Modifier) {
    val hata = LocalDurumRenkleri.current.hata
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(hata.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Rounded.Error, contentDescription = null, tint = hata, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(mesaj, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun NotKutusu(mesaj: String, modifier: Modifier = Modifier, simge: ImageVector = Icons.Rounded.Info, renk: Color = Color.Unspecified) {
    val vurgu = if (renk == Color.Unspecified) MaterialTheme.colorScheme.secondary else renk
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(vurgu.copy(alpha = 0.10f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(simge, contentDescription = null, tint = vurgu, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(mesaj, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun YukleniyorSatiri(metin: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text(metin, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DurumRozeti(metin: String, renk: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(renk.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(renk)
        )
        Spacer(Modifier.width(6.dp))
        Text(metin, style = MaterialTheme.typography.labelMedium, color = renk)
    }
}

@Composable
fun BolumBasligi(metin: String, modifier: Modifier = Modifier) {
    Text(
        metin.uppercase(com.furkan.network.utils.Bicim.TR),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
    )
}

@Composable
fun AnaButon(
    metin: String,
    simge: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
    tehlikeli: Boolean = false
) {
    val renkler = if (tehlikeli) {
        ButtonDefaults.buttonColors(
            containerColor = LocalDurumRenkleri.current.hata,
            contentColor = MaterialTheme.colorScheme.surface
        )
    } else {
        ButtonDefaults.buttonColors()
    }
    Button(
        onClick = onClick,
        enabled = etkin,
        colors = renkler,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = modifier
    ) {
        Icon(simge, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(metin)
    }
}

@Composable
fun IkincilButon(
    metin: String,
    simge: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = etkin,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
    ) {
        Icon(simge, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(metin)
    }
}

/** Alan adı / IP giriş kutusu: URL klavyesi, otomatik düzeltme kapalı, "Git" ile çalıştırır. */
@Composable
fun HostAlani(
    deger: String,
    degisti: (String) -> Unit,
    etiket: String,
    ornek: String,
    calistir: () -> Unit,
    modifier: Modifier = Modifier,
    etkin: Boolean = true,
    hata: String? = null
) {
    val odak = LocalFocusManager.current
    OutlinedTextField(
        value = deger,
        onValueChange = degisti,
        label = { Text(etiket) },
        placeholder = { Text(ornek) },
        singleLine = true,
        enabled = etkin,
        isError = hata != null,
        supportingText = if (hata != null) {
            { Text(hata) }
        } else {
            null
        },
        trailingIcon = if (deger.isNotEmpty() && etkin) {
            {
                IconButton(onClick = { degisti("") }) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Temizle")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go
        ),
        keyboardActions = KeyboardActions(onGo = {
            odak.clearFocus()
            calistir()
        }),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    )
}

/** Araç listesi satırı. */
@Composable
fun AracSatiri(
    baslik: String,
    aciklama: String,
    simge: ImageVector,
    onClick: () -> Unit,
    rozet: String? = null
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(simge, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(baslik, style = MaterialTheme.typography.titleSmall)
                    if (rozet != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            rozet,
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalDurumRenkleri.current.basari,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LocalDurumRenkleri.current.basari.copy(alpha = 0.14f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    aciklama,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Panel için kare hızlı erişim düğmesi. */
@Composable
fun HizliErisim(baslik: String, simge: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(simge, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(6.dp))
        Text(
            baslik,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Sinyal seviyesini 4 çubukla gösterir (0..4). */
@Composable
fun SinyalCubuklari(seviye: Int?, modifier: Modifier = Modifier) {
    val aktif = MaterialTheme.colorScheme.secondary
    val pasif = MaterialTheme.colorScheme.outline
    Row(modifier = modifier.height(18.dp), verticalAlignment = Alignment.Bottom) {
        for (i in 1..4) {
            Box(
                Modifier
                    .padding(horizontal = 1.5.dp)
                    .width(5.dp)
                    .height((4 + i * 3.5f).dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(if (seviye != null && i <= seviye) aktif else pasif)
            )
        }
    }
}
