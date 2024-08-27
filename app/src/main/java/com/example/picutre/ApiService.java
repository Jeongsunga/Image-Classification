package com.example.picutre;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/data")
    Call<ResponseData> sendData(@Body DataModel data);
}
