package com.app.chartapplication.di

import com.app.chartapplication.BuildConfig
import com.app.chartapplication.model.server.Api
import com.app.chartapplication.model.server.interceptor.ErrorResponseInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*


@Module
public class NetworkModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient {


        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {
                }
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.getSocketFactory()

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(object : HostnameVerifier {
                override fun verify(hostname: String, session: SSLSession): Boolean {
                    return true
                }
            })

            if (BuildConfig.DEBUG) {
                val httpLogInterceptor = HttpLoggingInterceptor()
                httpLogInterceptor.level = HttpLoggingInterceptor.Level.BODY
                builder.addNetworkInterceptor(httpLogInterceptor)
            }

            builder.addNetworkInterceptor(ErrorResponseInterceptor())

            return builder.connectionSpecs(Arrays.asList(ConnectionSpec.COMPATIBLE_TLS)).build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat(Api.SERVER_DATE_FORMAT)
            .serializeNulls()
            .create()
    }

    @Provides
    fun provideApi(okHttpClient: OkHttpClient, gson: Gson): Api {
        val serverPath = "https://demo.bankplus.ru"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .baseUrl(serverPath)
            .build()
            .create(Api::class.java)
    }
}