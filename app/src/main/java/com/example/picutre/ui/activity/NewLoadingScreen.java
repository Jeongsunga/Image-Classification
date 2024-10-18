package com.example.picutre.ui.activity;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.ResponseData;
import com.example.picutre.network.interfaces.FolderNameApi;
import com.example.picutre.network.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class NewLoadingScreen extends AppCompatActivity {

    private FolderNameApi folderNameApi;
    String baseUrl = BaseURL.BASE_URL;
    Retrofit retrofit = RetrofitClient.getClient(baseUrl);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_loading_screen);

        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");

        folderNameApi = retrofit.create(FolderNameApi.class);
        Call<ResponseData> call = folderNameApi.classifyFolder(folderName);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewLoadingScreen.this);
                    builder.setTitle("분류 완료")
                            .setMessage("분류 결과를 확인 하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent1 = new Intent(NewLoadingScreen.this, InAppGallery.class);
                                    startActivity(intent1);
                                }
                            })
                            .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent1 = new Intent(NewLoadingScreen.this, MainActivity.class);
                                    startActivity(intent1);
                                }
                            }).create().show();

                }else {
                    Toast.makeText(NewLoadingScreen.this, "분류 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(NewLoadingScreen.this, "분류 실패", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "분류 실패 " + t.getMessage());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}