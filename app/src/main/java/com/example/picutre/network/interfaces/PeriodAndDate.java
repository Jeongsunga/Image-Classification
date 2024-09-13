package com.example.picutre.network.interfaces;

import com.example.picutre.model.DateFolderName;
import com.example.picutre.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PeriodAndDate {
    @POST("/filterNumber/date")
    Call<ResponseData> sendData(@Body DateFolderName dataFolderName);
}
