package com.example.picutre.ui.activity;
// 서버에 저장된 분류 완료 결과들 중에서 사용자가 선택한 폴더의
// 폴더 이름, 사진 장 수, 사진들을 보여주는 화면(3번 화면)


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.example.picutre.constants.BaseURL;
import com.example.picutre.network.interfaces.ApiService;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.adapter.ImageAdapter;
import com.example.picutre.R;
import com.example.picutre.network.interfaces.ImageApi;

import java.util.ArrayList;
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
    String BASE_URL = BaseURL.BASE_URL;
    private ImageApi imageApi;
    private static final int DELETE_PHOTO_REQUEST_CODE = 1001;
    //private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_storage_images);

        //swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        gridView = findViewById(R.id.gridView);
        glideRequestManager = Glide.with(this);
        imagecount = findViewById(R.id.imageCount);
        foldername = findViewById(R.id.foldername);
        imageButton = findViewById(R.id.btn_menu);

        Retrofit retrofit = RetrofitClient.getClient(BASE_URL);
        imageApi = retrofit.create(ImageApi.class);

        // Intent에서 폴더 이름을 가져옵니다.
        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");
        int imageCount = intent.getIntExtra("imageCount", 0);

        foldername.setText(folderName);
        imagecount.setText(String.valueOf(imageCount));
        loadImages(folderName);

        // SwipeRefreshLayout의 리스너 설정
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // 새로고침 시 실행할 동작을 정의합니다.
//                //refreshData();
//            }
//        });
        
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DELETE_PHOTO_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                //Toast.makeText(FirebaseStorage_images.this, "이전 액티비티에서 값을 받지 못했습니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "사용자가 한장만 보이는 화면에서 아무런 동작 수행하지 않음.");
                return;
            }

            int imageCount = data.getIntExtra("imageCount", 0);
            ArrayList<String> imageLinks = data.getStringArrayListExtra("imageLinks");
            Log.d(TAG, "사진장수: " + imageCount + " 이미지 링크 리스트: " + imageLinks);

            // 사진 장수 업데이트
            imagecount.setText(String.valueOf(imageCount));

            // GridView 업데이트
            if (imageLinks != null) {
                imageAdapter.updateData(imageLinks);  // 어댑터의 데이터 갱신 메서드 호출
            } else {
                Toast.makeText(FirebaseStorage_images.this, "남은 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}