package com.project.rootsync.di

import com.project.rootsync.service.MqttService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MqttModule {

    @Provides
    @Singleton
    fun provideMqttService(): MqttService = MqttService()
}
