package com.codit.cryptoconverter.data.remote

import com.codit.cryptoconverter.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL_MARKET = "https://min-api.cryptocompare.com/"
    private const val TIMEOUT_SECONDS = 60L

    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    fun provideRetrofit(okHttp: OkHttpClient = provideOkHttp()): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_MARKET)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun provideMarketApi(retrofit: Retrofit = provideRetrofit()): MarketApiService =
        retrofit.create(MarketApiService::class.java)

    fun apiKeyOrNull(): String? {
        val key = try {
            BuildConfig.CRYPTOCOMPARE_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return key.ifEmpty { null }
    }
}
