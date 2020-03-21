package br.com.motoflash.courier.di.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by lucascordeiro on 10/09/2019.
 */

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        return GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
            .create()
    }

    @Provides
    @Named(BASE_URL)
    internal fun provideBaseUrl(): String {
//        return BuildConfig.BASE
        return ""
    }

    @Provides
    @Named(ACCEPT)
    internal fun provideAccept(): String {
        return "application/vnd.br.com.helpie.v1+json"
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            //                .authenticator(authenticator)
            .build()
    }

    companion object {
        const val BASE_URL = "baseUrl"
        const val ACCEPT = "accept"
    }
}
