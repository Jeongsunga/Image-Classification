package com.example.picutre;
// 파이어베이스 스토리지에 있는 폴더들을 보여주는 클래스(2번 화면)


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class inAppGallery extends AppCompatActivity {

    private ImageButton imageButton;
    private RecyclerView recyclerView;
    private StorageAdaptor storageAdaptor;
    private List<StorageItem> storageItemList;
    private static final String TAG = "inAppGallery";
    private OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_app_gallery);

        recyclerView = findViewById(R.id.recylcerview);
        imageButton = findViewById(R.id.btn_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storageItemList = new ArrayList<>();
        storageAdaptor = new StorageAdaptor(storageItemList);
        recyclerView.setAdapter(storageAdaptor);

        fetchDataFromServer();

        //fetchStorageItems();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(inAppGallery.this, imageButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(inAppGallery.this, item.getTitle(), Toast.LENGTH_SHORT).show();
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

    private void fetchDataFromServer() {
        Request request = new Request.Builder()
                .url("http://172.21.195.40:5000/get/folderList") // Flask 서버의 URL
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

    private void parseJsonData(String jsonData) throws Exception {
        JSONArray jsonArray = new JSONArray(jsonData);
        storageItemList.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String folderName = jsonObject.getString("folder_name");
            int photoCount = jsonObject.getInt("photo_count");
            String firstPhotoPath = jsonObject.optString("first_photo", "default.jpg");
            String firstPhotoUrl = "http://172.21.195.40:5000/get/folderList/" + firstPhotoPath;
            storageItemList.add(new StorageItem(folderName, firstPhotoUrl, photoCount));
        }

        storageAdaptor.notifyDataSetChanged();
    }

    /*private void fetchStorageItems() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {

            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    fetchFolderDetails(prefix);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "no data");
                Toast.makeText(inAppGallery.this, "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                //Log.e("MainActivity", "Failed to fetch folders", e);
            }
        });
    }

    private void fetchFolderDetails(StorageReference folderRef) {

        folderRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                //
                String folderName = folderRef.getName();
                //String firstImagePath = null;
                int count = listResult.getItems().size();

                if (!listResult.getItems().isEmpty()) {

                    StorageReference firstImageRef = listResult.getItems().get(0);
                    //firstImagePath = listResult.getItems().get(0).getPath();
                    firstImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String firstImagePath = uri.toString();
                            StorageItem storageItem = new StorageItem(folderName, firstImagePath, count);
                            storageItemList.add(storageItem);
                            storageAdaptor.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("MainActivity", "Failed to get download URL", e);
                        }
                    });
                } else {
                    StorageItem storageItem = new StorageItem(folderName, null, count);
                    storageItemList.add(storageItem);
                    storageAdaptor.notifyDataSetChanged();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.e("MainActivity", "Failed to fetch folder details", e);

            }
        });
    }*/
}