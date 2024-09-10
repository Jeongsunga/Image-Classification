package com.example.picutre;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeleteImageApi {
    @POST("/delete-image")
    Call<DeleteImage> sendUrl(@Body String image_url);
}
