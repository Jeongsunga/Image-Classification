package com.example.picutre.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.StorageItem;
import com.example.picutre.ui.adapter.ServerFolderAdapter;
import com.example.picutre.ui.adapter.StorageAdaptor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerFolderList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServerFolderAdapter serverFolderAdapter;
    private List<StorageItem> storageItemList;
    private OkHttpClient client = new OkHttpClient();
    private String baseUrl = BaseURL.BASE_URL;
    private static final String TAG = "serverFolderList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_server_folder_list);

        recyclerView = findViewById(R.id.recylcerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storageItemList = new ArrayList<>();
        serverFolderAdapter = new ServerFolderAdapter(storageItemList);
        recyclerView.setAdapter(serverFolderAdapter);
        //storageAdaptor = new StorageAdaptor(storageItemList);
        //recyclerView.setAdapter(storageAdaptor);

        fetchDataFromServer();

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

        serverFolderAdapter.notifyDataSetChanged();
    }
}