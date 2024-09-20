package com.example.picutre.network.interfaces;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImageApi {
    @GET("get_images/{folderName}")
    Call<List<String>> getImages(@Path("folderName") String folderName);
}
