package com.example.admobinappbilling

import android.app.Application
import com.google.android.gms.ads.MobileAds


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)
    }
}