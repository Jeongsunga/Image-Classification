package com.example.picutre;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FilterNumber {

    @POST("/filterNumber")
    Call<ResponseData> sendData(@Body OnlyFilterNumber data);
}
