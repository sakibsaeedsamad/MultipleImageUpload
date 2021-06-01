package com.sssakib.multipleimageupload

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitClient private constructor() {
    private val retrofit: Retrofit
    val aPI: API
        get() = retrofit.create(API::class.java)

    companion object {
        private const val BASE_URL = " http://192.168.0.104:8080/"
        private var retrofitClient: RetrofitClient? = null

        @get:Synchronized
        val instance: RetrofitClient?
            get() {
                if (retrofitClient == null) {
                    retrofitClient = RetrofitClient()
                }
                return retrofitClient
            }
    }

    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
}