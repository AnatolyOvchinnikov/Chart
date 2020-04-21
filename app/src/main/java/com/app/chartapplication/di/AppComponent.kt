package com.app.chartapplication.di

import com.app.chartapplication.presentation.ChartViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface AppComponent {
    fun inject(viewModel: ChartViewModel)
}