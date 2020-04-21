package com.app.chartapplication.model.server

import com.app.chartapplication.entity.ServerResponse
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
interface Api {
    companion object {
        public const val SERVER_DATE_FORMAT = "yyyy-MMM-dd hh:mm:ss a ZZ"
    }

    @FormUrlEncoded
    @POST("/mobws/json/pointsList")
    fun requestCoordsList(
        @Field("count") count: Int, @Field("version") version: Float
    ): Single<ServerResponse>
}