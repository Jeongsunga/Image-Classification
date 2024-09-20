package com.example.picutre.ui.activity;
// 서버에 저장된 분류 완료 결과들 중에서 사용자가 선택한 폴더의
// 폴더 이름, 사진 장 수, 사진들을 보여주는 화면(3번 화면)


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.adapter.ImageAdapter;
import com.example.picutre.R;
import com.example.picutre.network.interfaces.ImageApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FirebaseStorage_images extends AppCompatActivity {
    private GridView gridView;
    private ImageAdapter imageAdapter;
    private RequestManager glideRequestManager;
    private TextView imagecount, foldername;
    private ImageButton imageButton;
    String BASE_URL = BaseURL.BASE_URL;
    private ImageApi imageApi;
    private static final int DELETE_PHOTO_REQUEST_CODE = 1001;
    private List<String> favoriteImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_storage_images);

        gridView = findViewById(R.id.gridView);
        glideRequestManager = Glide.with(this);
        imagecount = findViewById(R.id.imageCount);
        foldername = findViewById(R.id.foldername);
        imageButton = findViewById(R.id.likely);

        Retrofit retrofit = RetrofitClient.getClient(BASE_URL);
        imageApi = retrofit.create(ImageApi.class);

        // Intent에서 폴더 이름을 가져옵니다.
        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");
        int imageCount = intent.getIntExtra("imageCount", 0);

        foldername.setText(folderName);
        imagecount.setText(String.valueOf(imageCount));
        loadImages(folderName);

        // SharedPreferences에 데이터 저장 (FirebaseStorage_images와 imageSliderAdapter에서 각각 저장 가능)
        SharedPreferences sharedPreferences = getSharedPreferences("folderName", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("foldername", folderName);
        editor.apply();
        
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable currentDrawable = imageButton.getDrawable();
                Drawable expectedDrawable = ContextCompat.getDrawable(FirebaseStorage_images.this, R.drawable.heart);

                // 이미지 버튼의 상태가 heart에서 누르면 즐겨찾기 목록이 나오게 한다.
                if (currentDrawable.getConstantState() != null && currentDrawable.getConstantState().equals(expectedDrawable.getConstantState())) {
                    imageButton.setImageResource(R.drawable.fullheart);
                    firebaseHandler(folderName, 3);
                }
                // 이미지 버튼의 상태가 fullheart에서 누르면 원래 목록이 나오게 한다.
                else {
                    imageButton.setImageResource(R.drawable.heart);
                    firebaseHandler(folderName, 4);
                }
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
                        imagecount.setText(String.valueOf(imageUrls.size()));
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

    public void firebaseHandler(String folderName, int num) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference(folderName);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists() || !snapshot.hasChildren()) {
                    Toast.makeText(FirebaseStorage_images.this, "파이어베이스에 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }else {
                    if(num == 3){
                        // 좋아요 누른 사진만 보이게 하는 구문 실행
                        favoriteImageUrls.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //반복문으로 데이터 리스트 추출
                            if(dataSnapshot.getValue(Boolean.class)) {
                                String url = decodeUrl(dataSnapshot.getKey());
                                favoriteImageUrls.add(url);
                            }
                        }
                        imageAdapter.updateData(favoriteImageUrls);  // 어댑터의 데이터 갱신 메서드 호출
                        imagecount.setText(String.valueOf(favoriteImageUrls.size()));
                    }else { // 원래 화면 보여주기
                        loadImages(folderName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FirebaseStorage_images.this, "실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String decodeUrl(String encodedUrl) {
        byte[] decodedBytes = Base64.decode(encodedUrl, Base64.NO_WRAP);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}