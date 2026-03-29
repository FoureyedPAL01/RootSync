package com.project.rootsync.di

import com.project.rootsync.data.remote.AirQualityApiService
import com.project.rootsync.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAirQualityApiService(): AirQualityApiService {
        return Retrofit.Builder()
            .baseUrl(AirQualityApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirQualityApiService::class.java)
    }
}
