package com.tiantian.wardrobe

import android.app.Application
import com.tiantian.wardrobe.data.AppDatabase

class TianTianApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}
