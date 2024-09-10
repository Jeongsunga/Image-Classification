package com.example.picutre;
// 서버에 저장된 분류 완료 결과들 중에서 사용자가 선택한 폴더의
// 폴더 이름, 사진 장 수, 사진들을 보여주는 화면(3번 화면)


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FirebaseStorage_images extends AppCompatActivity {
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private RequestManager glideRequestManager;
    private TextView imagecount, foldername;

    private ImageButton imageButton;
    private static final String BASE_URL = "http://192.168.7.10:5000/";
    private ImageApi imageApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_storage_images);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)  // 로컬 호스트 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gridView = findViewById(R.id.gridView);
        glideRequestManager = Glide.with(this);
        imagecount = findViewById(R.id.imageCount);
        foldername = findViewById(R.id.foldername);
        imageButton = findViewById(R.id.btn_menu);
        imageApi = retrofit.create(ImageApi.class);

        // Intent에서 폴더 이름을 가져옵니다.
        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");
        int imageCount = intent.getIntExtra("imageCount", 0);

        foldername.setText(folderName);
        imagecount.setText(String.valueOf(imageCount));
        loadImages(folderName);
        
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(FirebaseStorage_images.this, imageButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(FirebaseStorage_images.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void loadImages(String folderName) {
        Call<List<String>> call = imageApi.getImages(folderName);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if(response.isSuccessful()) {
                    if(response.body() != null) {
                        List<String> imageUrls = response.body();
                        imageAdapter = new ImageAdapter(FirebaseStorage_images.this, imageUrls, glideRequestManager);
                        gridView.setAdapter(imageAdapter);
                    }else {
                        Log.d(TAG, "Body가 비었습니다.");
                    }
                }else {
                    Log.d(TAG, "응답에 실패하였습니다.");
                }

            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.d(TAG, "Error20010425: " + t.getMessage());
            }
        });
    }
}