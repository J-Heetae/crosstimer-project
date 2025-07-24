package com.goose.crosstimerapp.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CrossroadService {
    @POST("/crossroad")
    fun getCrossroadDataInRange(@Body request: CrossroadRequest): Call<List<CrossroadResponse>>

}