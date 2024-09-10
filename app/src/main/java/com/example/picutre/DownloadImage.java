package com.example.picutre;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DownloadImage {
    @GET
    Call<ResponseBody> downloadImage(@Url String imageUrl);
}
