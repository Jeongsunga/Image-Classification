package com.example.picutre.network.interfaces;

import com.example.picutre.model.Metadatas;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ImageUrlApi {
    // 서버에 메타데이터를 받을 이미지 링크를 보내는 api
    @POST("/image_metadata")
    Call<Metadatas> sendAPI(@Body String image_url);
}
