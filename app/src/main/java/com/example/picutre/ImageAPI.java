package com.example.picutre;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ImageAPI {
    @GET("get_images")
    Call<List<ImageResponse>> getImages(@Query("folder") String folderName);
}
