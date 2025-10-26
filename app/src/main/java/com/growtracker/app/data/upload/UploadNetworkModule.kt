package com.growtracker.app.data.upload

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.CertificatePinner
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.growtracker.app.BuildConfig

object UploadNetworkModule {
    @Volatile private var retrofit: Retrofit? = null

    fun api(context: Context, baseUrl: String): IngestApi {
        val instance = retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit(baseUrl).also { retrofit = it }
        }
        return instance.create(IngestApi::class.java)
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            // In Debug, log bodies to aid development; in Release, disable logging entirely
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val headers = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .header("X-App-Version", BuildConfig.VERSION_NAME)
                .build()
            chain.proceed(req)
        }
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(headers)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
        // Optional Certificate Pinning via BuildConfig
        val host = BuildConfig.PIN_HOST?.trim().orEmpty()
        val pinsCsv = BuildConfig.PIN_SHA256S?.trim().orEmpty()
        if (host.isNotEmpty() && pinsCsv.isNotEmpty()) {
            val pinnerBuilder = CertificatePinner.Builder()
            pinsCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() }.forEach { pin ->
                // OkHttp expects pins formatted as "sha256/BASE64..."
                pinnerBuilder.add(host, pin)
            }
            clientBuilder.certificatePinner(pinnerBuilder.build())
        }
        val client = clientBuilder.build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
}
