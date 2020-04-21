package com.app.chartapplication.model.server.interceptor

import com.app.chartapplication.App
import com.app.chartapplication.R
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
class ErrorResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())
        val code = response.code
        if(!response.isSuccessful) {
            throw Exception(App.applicationContext().getString(R.string.unknown_error))
        } else {
            val str = response.body?.string()
            val gson = Gson()
            val error: Error? = gson.fromJson(str, Error::class.java)
            if(error?.response?.result != null && error.response.message != null) {
                throw Exception(error.response.message)
            } else {
                response = response.newBuilder().body(ResponseBody.create(response.body?.contentType(), str!!)).build();
            }
        }

        return response
    }

    data class Error(val response: ErrorResponse)
    data class ErrorResponse(val result: Int? = null, val message: String? = null)

    private fun checkResultCode() {

    }
}