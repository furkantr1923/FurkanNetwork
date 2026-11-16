package com.furkan.network

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.furkan.network.ui.navigation.AnaNavigasyon
import com.furkan.network.ui.theme.FurkanNetworkTema

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val konteyner = (application as FurkanNetworkApp).konteyner

        setContent {
            val ayarlar by konteyner.ayarlar.durum.collectAsStateWithLifecycle()
            val koyu = ayarlar.koyuTema

            // Durum/gezinti çubuğu simge renklerini temaya göre ayarla.
            DisposableEffect(koyu) {
                val stil = if (koyu) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                this@MainActivity.enableEdgeToEdge(statusBarStyle = stil, navigationBarStyle = stil)
                onDispose { }
            }

            FurkanNetworkTema(koyuTema = koyu) {
                AnaNavigasyon(konteyner)
            }
        }
    }
}
