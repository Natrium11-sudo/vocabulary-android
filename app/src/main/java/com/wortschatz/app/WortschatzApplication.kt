package com.wortschatz.app

import android.app.Application

class WortschatzApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
