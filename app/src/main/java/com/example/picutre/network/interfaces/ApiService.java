package com.example.picutre.network.interfaces;

import com.example.picutre.model.DataModel;
import com.example.picutre.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/data")
    Call<ResponseData> sendData(@Body DataModel data);
}
