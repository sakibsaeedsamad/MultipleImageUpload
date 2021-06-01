package com.sssakib.multipleimageupload


import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.nio.file.Files


interface API {


//    @GET("allFiles")
//    fun findAllFiles(): Call<List<DBFile?>?>?


    @Multipart
    @POST("uploadMultipleFiles")
    fun uploadImages(@Part files: List<MultipartBody.Part?>? ): Call<ResponseBody>
}
