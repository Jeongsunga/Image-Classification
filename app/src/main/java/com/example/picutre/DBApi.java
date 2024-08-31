package com.example.picutre;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DBApi {
    @POST("/upload")
    Call<ResponseData> sendData(@Body DBupload data);
}
