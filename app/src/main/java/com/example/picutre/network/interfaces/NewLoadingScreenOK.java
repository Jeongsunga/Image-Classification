package com.example.picutre.network.interfaces;

import com.example.picutre.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NewLoadingScreenOK {

    @GET("get-ok")
    Call<ResponseData> getOkay();
}
