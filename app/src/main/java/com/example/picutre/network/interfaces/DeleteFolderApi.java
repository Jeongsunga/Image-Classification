package com.example.picutre.network.interfaces;

import com.example.picutre.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeleteFolderApi {
    @POST("delete-folder")
    Call<ResponseData> deleteFolder(@Body String folderName);
}
