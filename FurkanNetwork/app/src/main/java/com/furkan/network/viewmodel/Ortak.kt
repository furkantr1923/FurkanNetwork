package com.furkan.network.viewmodel

import androidx.lifecycle.AndroidViewModel
import com.furkan.network.FurkanNetworkApp
import com.furkan.network.data.UygulamaKonteyneri

/** ViewModel'lerden servis kabına erişim. */
val AndroidViewModel.konteyner: UygulamaKonteyneri
    get() = getApplication<FurkanNetworkApp>().konteyner
