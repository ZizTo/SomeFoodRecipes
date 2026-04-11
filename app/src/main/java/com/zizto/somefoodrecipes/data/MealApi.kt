package com.zizto.somefoodrecipes.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {
    @GET("api/json/v1/1/search.php")
    suspend fun searchMeals(@Query("s") query: String): MealResponse

    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealDetails(@Query("i") id: String): MealResponse

    companion object {
        private const val BASE_URL = "https://www.themealdb.com/"

        fun create(): MealApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MealApi::class.java)
        }
    }
}