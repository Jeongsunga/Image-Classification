package com.example.picutre.network.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ImageDeleteService {
    @POST("/multi-delete")
    Call<Void> deleteImages(@Body List<String> imageUrls);
}
