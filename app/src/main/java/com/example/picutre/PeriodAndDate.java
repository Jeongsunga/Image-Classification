package com.example.picutre;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PeriodAndDate {
    @POST("/filterNumber/date")
    Call<ResponseData> sendData(@Body DateFolderName dataFolderName);
}
