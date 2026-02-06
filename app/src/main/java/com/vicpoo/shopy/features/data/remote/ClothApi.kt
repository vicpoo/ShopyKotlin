package com.vicpoo.shopy.features.data.remote

import com.vicpoo.shopy.features.data.dto.ClothDto
import com.vicpoo.shopy.features.data.dto.ClothRequestDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ClothApi {
    @GET("api/clothes")
    suspend fun getAllClothes(): List<ClothDto>

    @GET("api/clothes/{id}")
    suspend fun getClothById(@Path("id") id: Int): ClothDto

    @Multipart
    @POST("api/clothes")
    suspend fun createCloth(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("size") size: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("stock") stock: RequestBody? = null,
        @Part image: MultipartBody.Part? = null
    ): ClothDto

    @Multipart
    @PUT("api/clothes/{id}")
    suspend fun updateCloth(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("size") size: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("stock") stock: RequestBody? = null,
        @Part image: MultipartBody.Part? = null,
        @Part("existing_image_url") existingImageUrl: RequestBody? = null
    ): ClothDto

    @DELETE("api/clothes/{id}")
    suspend fun deleteCloth(@Path("id") id: Int): Response<Void>

    @GET("api/clothes/search/name")
    suspend fun searchByName(@Query("name") name: String): List<ClothDto>

    @GET("api/clothes/search/size")
    suspend fun searchBySize(@Query("size") size: String): List<ClothDto>

    @GET("api/clothes/search/price")
    suspend fun searchByPriceRange(
        @Query("min_price") minPrice: Double,
        @Query("max_price") maxPrice: Double
    ): List<ClothDto>
}