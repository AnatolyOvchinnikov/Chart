package com.app.chartapplication

import android.app.Application
import android.content.Context
import com.app.chartapplication.di.AppComponent
import com.app.chartapplication.di.DaggerAppComponent
import com.app.chartapplication.di.NetworkModule

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
class App : Application() {
    companion object {
        private var instance: App? = null
        var component: AppComponent? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
        fun getInstance() = instance
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
            .networkModule(NetworkModule())
            .build()
    }
}