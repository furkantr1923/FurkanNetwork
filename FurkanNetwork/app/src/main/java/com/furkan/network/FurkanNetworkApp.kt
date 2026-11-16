package com.furkan.network

import android.app.Application
import com.furkan.network.data.UygulamaKonteyneri

class FurkanNetworkApp : Application() {

    lateinit var konteyner: UygulamaKonteyneri
        private set

    override fun onCreate() {
        super.onCreate()
        konteyner = UygulamaKonteyneri(this)
    }
}
