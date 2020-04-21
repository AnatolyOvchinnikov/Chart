package com.app.chartapplication.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Anatoly Ovchinnikov on 2020-04-21.
 */
data class Coord(val x: Float, val y: Float)

data class ServerResponse(@SerializedName("result") val result: Int,
                          @SerializedName("response") val response: PointsResponseList) : Serializable

data class PointsResponseList(@SerializedName("points") val points: List<Coord>) : Serializable