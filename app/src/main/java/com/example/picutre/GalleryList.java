package com.example.picutre;
//스마트폰의 갤러리 폴더들을 보여주는 화면(3번 화면)
// 이 화면에서 사용자가 분류할 이미지 폴더를 선택한다.


import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GalleryList extends AppCompatActivity  {

    private static final int REQUEST_PERMISSION = 100;
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderItem> folderItems;
    public String firstImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery_list);

        recyclerView = findViewById(R.id.recylcerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        folderItems = new ArrayList<>();

        loadGalleryFolders();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadGalleryFolders() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN
        };
        //String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            Map<String, Integer> folderCountMap = new HashMap<>();
            Map<String, String> folderMap = new LinkedHashMap<>();
            //Map<String, Long> folderDateMap = new HashMap<>();

            while (cursor.moveToNext()) {
                String folderName = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                firstImagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                String folderPath = new File(firstImagePath).getParent(); // 이미지 경로에서 폴더 경로 추출
                //Log.d(TAG, "이미지 데이터? " + folderPath);

                //long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                if (!folderMap.containsKey(folderName)) {
                    folderMap.put(folderName, firstImagePath);
                    //folderDateMap.put(folderName, dateTaken);
                }

                //각 폴더별 사진 수 세기
                if(folderCountMap.containsKey(folderName)) {
                    int count = folderCountMap.get(folderName);
                    folderCountMap.put(folderName, count+1);
                }else {
                    folderCountMap.put(folderName, 1);
                    folderMap.put(folderName, firstImagePath);
                }

            }
            cursor.close();

            for(Map.Entry<String, Integer> entry : folderCountMap.entrySet()) {
                String folderName = entry.getKey();
                int count = entry.getValue();
                String firstImagePath = folderMap.get(folderName);
                folderItems.add(new FolderItem(folderName, firstImagePath, count));
            }

            adapter = new FolderAdapter(folderItems);
            recyclerView.setAdapter(adapter);


        }
    }


}