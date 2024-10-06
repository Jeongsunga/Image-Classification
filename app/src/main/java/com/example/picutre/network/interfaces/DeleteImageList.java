package com.example.picutre.network.interfaces;

import com.example.picutre.model.DeleteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeleteImageList {
    @POST("/delete-images")
    Call<DeleteResponse> deleteImageList(@Body List<String> image_urls);
}
