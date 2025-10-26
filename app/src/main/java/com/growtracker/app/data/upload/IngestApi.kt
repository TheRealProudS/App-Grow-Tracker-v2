package com.growtracker.app.data.upload

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface IngestApi {
    @Multipart
    @POST("/v1/ingest/image")
    suspend fun uploadImage(
        @Header("X-Play-Integrity") integrityToken: String?,
        @Part image: MultipartBody.Part,
        @Part("meta") metaJson: RequestBody
    ): Response<ResponseBody>
}
