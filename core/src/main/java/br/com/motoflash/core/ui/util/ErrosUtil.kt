package br.com.motoflash.core.ui.util

import br.com.motoflash.core.BuildConfig
import br.com.motoflash.core.data.network.model.ErrorCode
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class ErrosUtil() {
    companion object{
       fun getErrorCode(e: HttpException) : ErrorCode {
           val response = e.response()
           val converter = GsonConverterFactory.create().responseBodyConverter(HashMap::class.java, arrayOf(), retrofit2.Retrofit.Builder()
               .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
               .addConverterFactory(GsonConverterFactory.create())
               .baseUrl(BuildConfig.BASE_URL)
               .build())!!
           val error = converter.convert(response.errorBody())!! as HashMap<String, Any>
           return ErrorCode(
               code = error["code"].toString().unMaskOnlyNumbers().toLong(),
               type = error["type"].toString(),
               message = error["message"].toString(),
               field = error["field"].toString()
           )
       }
    }
}