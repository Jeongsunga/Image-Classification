package com.example.picutre.network.interfaces;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface NewZipApi {
    @Multipart
    @POST("post/folderZip")
    Call<ResponseBody> uploadFile(@Part MultipartBody.Part uploaded_file);
}
