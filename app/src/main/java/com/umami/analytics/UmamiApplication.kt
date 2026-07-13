package com.umami.analytics

import android.app.Application
import com.umami.analytics.data.api.UmamiApiService
import com.umami.analytics.data.db.UmamiDatabase
import com.umami.analytics.data.preferences.SessionManager
import com.umami.analytics.util.NetworkObserver

class UmamiApplication : Application() {

    lateinit var database: UmamiDatabase
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var apiService: UmamiApiService
        private set

    lateinit var networkObserver: NetworkObserver
        private set

    override fun onCreate() {
        super.onCreate()
        database = UmamiDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        apiService = UmamiApiService()
        networkObserver = NetworkObserver(this)
    }
}
