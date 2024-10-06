package com.example.picutre.ui.activity;
// 서버에 있는 분류 완료 폴더들을 보여주는 클래스(2번 화면)


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.ui.adapter.StorageAdaptor;
import com.example.picutre.model.StorageItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class InAppGallery extends AppCompatActivity {

    private ImageButton imageButton;
    private RecyclerView recyclerView;
    private StorageAdaptor storageAdaptor;
    private List<StorageItem> storageItemList;
    private static final String TAG = "inAppGallery";
    private OkHttpClient client = new OkHttpClient();
    String baseUrl = BaseURL.BASE_URL;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_app_gallery);

        recyclerView = findViewById(R.id.recylcerview);
        imageButton = findViewById(R.id.btn_menu);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        imageButton.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storageItemList = new ArrayList<>();
        storageAdaptor = new StorageAdaptor(storageItemList);
        recyclerView.setAdapter(storageAdaptor);

        fetchDataFromServer();

        // 새로고침 기능 구현
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 새로고침 시 실행할 코드
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 어댑터 업데이트
                        fetchDataFromServer();
                        swipeRefreshLayout.setRefreshing(false); // 새로고침 완료
                    }
                }, 1000);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(InAppGallery.this, imageButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(InAppGallery.this, item.getTitle(), Toast.LENGTH_SHORT).show();
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

    public void fetchDataFromServer() {
        Request request = new Request.Builder()
                .url(baseUrl + "get/folderList") // Flask 서버의 URL
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch data", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            parseJsonData(responseData);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse JSON", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Server response error: " + response.code());
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void parseJsonData(String jsonData) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonData);
        storageItemList.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String folderName = jsonObject.getString("folder_name");
            int photoCount = jsonObject.getInt("photo_count");
            String firstPhotoPath = jsonObject.optString("first_photo", "default.jpg");
            String firstPhotoUrl = baseUrl + "get/folderList/" + firstPhotoPath;
            storageItemList.add(new StorageItem(folderName, firstPhotoUrl, photoCount));
        }

        storageAdaptor.notifyDataSetChanged();
    }

    public void downloadImage(@NonNull List<String> imageUrls, Context context) {
        ImageOne imageOne = new ImageOne();

        for(String imageUrl : imageUrls) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(imageUrl) // 이미지 URL
                    .build();

            // 이미지 다운로드
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(InAppGallery.this, "다운로드 실패", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try (InputStream inputStream = response.body().byteStream()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream); // Bitmap 생성

                            // 저장할 폴더 및 파일 경로 설정
                            String folderName = imageOne.getFolderNameFromUrl(imageUrl); // URL에서 폴더 이름 가져오기
                            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File folder = new File(externalStorageDir, folderName);
                            if (!folder.exists()) {
                                folder.mkdirs(); // 폴더가 없으면 생성
                            }

                            String imageName = imageOne.getImageNameFromUrl(imageUrl); // URL에서 이미지 이름 가져오기
                            File file = new File(folder, imageName);

                            // 이미지를 파일로 저장
                            try (OutputStream output = new FileOutputStream(file)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                            }

                            // 갤러리 업데이트
                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "응답 실패: " + response.message());
                    }
                }
            });
        }
        Log.d(TAG, "저장 완료");
    }
}