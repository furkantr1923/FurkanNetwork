package com.furkan.network.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.furkan.network.data.UygulamaKonteyneri
import com.furkan.network.screens.AgBilgiEkrani
import com.furkan.network.screens.AltAgEkrani
import com.furkan.network.screens.AraclarEkrani
import com.furkan.network.screens.AyarlarEkrani
import com.furkan.network.screens.DnsEkrani
import com.furkan.network.screens.GecmisEkrani
import com.furkan.network.screens.GizlilikEkrani
import com.furkan.network.screens.HakkindaEkrani
import com.furkan.network.screens.HashEkrani
import com.furkan.network.screens.HizTestiEkrani
import com.furkan.network.screens.IpEkrani
import com.furkan.network.screens.PanelEkrani
import com.furkan.network.screens.PingEkrani
import com.furkan.network.screens.SifreEkrani
import com.furkan.network.screens.SslEkrani
import com.furkan.network.screens.TracerouteEkrani

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnaNavigasyon(@Suppress("UNUSED_PARAMETER") konteyner: UygulamaKonteyneri) {
    val nav = rememberNavController()
    val girdi by nav.currentBackStackEntryAsState()
    val mevcut = Rota.yoldan(girdi?.destination?.route)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(mevcut.baslik, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    if (!mevcut.ustSeviye) {
                        IconButton(onClick = { nav.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (mevcut.ustSeviye) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    AltSekmeler.forEach { sekme ->
                        val secili = girdi?.destination?.hierarchy?.any { it.route == sekme.rota.yol } == true
                        NavigationBarItem(
                            selected = secili,
                            onClick = { sekmeyeGit(nav, sekme.rota) },
                            icon = { Icon(sekme.simge, contentDescription = null) },
                            label = { Text(sekme.etiket) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            )
                        )
                    }
                }
            }
        }
    ) { ic ->
        NavHost(
            navController = nav,
            startDestination = Rota.PANEL.yol,
            modifier = Modifier
                .padding(ic)
                .consumeWindowInsets(ic)
                .imePadding(),
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            val git: (Rota) -> Unit = { rota -> nav.navigate(rota.yol) { launchSingleTop = true } }
            composable(Rota.PANEL.yol) { PanelEkrani(git = git) }
            composable(Rota.ARACLAR.yol) { AraclarEkrani(git = git) }
            composable(Rota.GECMIS.yol) { GecmisEkrani() }
            composable(Rota.AYARLAR.yol) { AyarlarEkrani(git = git) }
            composable(Rota.IP.yol) { IpEkrani() }
            composable(Rota.DNS.yol) { DnsEkrani() }
            composable(Rota.PING.yol) { PingEkrani() }
            composable(Rota.TRACEROUTE.yol) { TracerouteEkrani() }
            composable(Rota.AG.yol) { AgBilgiEkrani() }
            composable(Rota.ALT_AG.yol) { AltAgEkrani() }
            composable(Rota.HASH.yol) { HashEkrani() }
            composable(Rota.SIFRE.yol) { SifreEkrani() }
            composable(Rota.SSL.yol) { SslEkrani() }
            composable(Rota.HIZ.yol) { HizTestiEkrani() }
            composable(Rota.GIZLILIK.yol) { GizlilikEkrani() }
            composable(Rota.HAKKINDA.yol) { HakkindaEkrani() }
        }
    }
}

private fun sekmeyeGit(nav: NavHostController, rota: Rota) {
    nav.navigate(rota.yol) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
