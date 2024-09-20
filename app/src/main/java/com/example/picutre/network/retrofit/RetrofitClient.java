package com.example.picutre.network.retrofit;

import com.example.picutre.constants.BaseURL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    String apiURL = BaseURL.BASE_URL;

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
