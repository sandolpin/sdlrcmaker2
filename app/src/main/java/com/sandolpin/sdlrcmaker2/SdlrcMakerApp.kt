package com.sandolpin.sdlrcmaker2

import android.app.Application

class SdlrcMakerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
