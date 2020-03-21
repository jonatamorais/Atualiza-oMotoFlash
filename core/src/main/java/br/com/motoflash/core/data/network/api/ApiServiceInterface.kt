package br.com.motoflash.core.data.network.api

import br.com.motoflash.core.BuildConfig
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.requery.Convert
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


interface ApiServiceInterface {

    //Client Start

    //Profile Start
    @POST("api/users")
    fun doCreateUser(
        @Body body: JsonObject
    ) : Observable<HashMap<String,Any>>

    @PUT("api/users/{userId}")
    fun doUpdateUser(
        @Header("accesstoken") accessToken: String,
        @Path("userId") userId: String,
        @Body body: JsonObject
    ): Observable<HashMap<String, Any>>

    @PUT("api/users/{userId}/password")
    fun doUpdateUserPassword(
        @Header("accesstoken") accessToken: String,
        @Path("userId") userId: String,
        @Body body: JsonObject
    ): Observable<HashMap<String, Any>>
    //Profile End

    //Quotation Start
    @POST("api/quotation")
    fun doCreateQuotation(
        @Header("accesstoken") accessToken: String,
        @Body body: JsonObject
    ): Observable<HashMap<String,Quotation>>
    //Quotation End

    //WorkOrder Start
    @POST("api/workorders")
    fun doCreateWorkOrder(
        @Header("accesstoken") accessToken: String,
        @Body body: JsonObject
    ): Observable<HashMap<String, WorkOrder>>

    @PUT("api/workorders/{workOrderId}/cancell")
    fun doCancellWorkOrder(
        @Header("accesstoken") accessToken: String,
        @Path("workOrderId") workOrderId: String
    ):Observable<HashMap<String,Any>>

    @POST("RunQueue/{workOrderId}")
    fun doRunQueue(
        @Header("accesstoken") accessToken: String,
        @Path("workOrderId") workOrderId: String
    ): Observable<HashMap<String, Any>>
    //WorkOrder End

    //Client End

    //Courier Start

    //Profile Start
    @POST("api/couriers")
    fun doCreateCourier(
        @Body body: JsonObject
    ) : Observable<HashMap<String, Any>>

    @PUT("api/couriers/{courierId}/online")
    fun doCourierOnline(
        @Header("accesstoken") accessToken: String,
        @Path("courierId") courierId: String,
        @Body body: JsonObject
    ) : Observable<HashMap<String, Any>>

    //Profile End

    //WorkOrder Start

    @PUT("api/workorders/{workOrderId}/courier/{courierId}")
    fun doAssingCourier(
        @Header("accesstoken") accessToken: String,
        @Path("courierId") courierId: String,
        @Path("workOrderId") workOrderId: String
    ):Observable<HashMap<String,Any>>

    @PUT("api/workorders/{workOrderId}/courier/{courierId}/denie")
    fun doDeniedCourier(
        @Header("accesstoken") accessToken: String,
        @Path("courierId") courierId: String,
        @Path("workOrderId") workOrderId: String
    ):Observable<HashMap<String,Any>>

    //WorkOrder End

    //Courier End

    companion object Factory {
        fun create(): ApiServiceInterface {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build()

            val retrofit = retrofit2.Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(BuildConfig.BASE_URL)
                .build()

            return retrofit.create(ApiServiceInterface::class.java)
        }
    }
}