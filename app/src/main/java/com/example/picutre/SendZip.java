package com.example.picutre;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SendZip {
    @Multipart
    @POST("/get/folderZip")
    Call<ResponseBody> uploadZipFile(@Part MultipartBody.Part file);
}
