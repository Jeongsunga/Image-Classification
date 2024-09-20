package com.example.picutre.ui.activity;
// 서버에 저장된 분류 완료 결과들 중에서 사용자가 선택한 폴더의
// 폴더 이름, 사진 장 수, 사진들을 보여주는 화면(3번 화면)


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.util.Base64;
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
    private boolean multiSelect = false; // 멀티 선택 모드인지 여부


    private List<String> imageUrls; // 이미지 URL 리스트
    private List<String> selectedImages = new ArrayList<>(); // 선택된 이미지 리스트


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_storage_images);

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

        // SharedPreferences에 데이터 저장 (FirebaseStorage_images와 imageSliderAdapter에서 각각 저장 가능)
        SharedPreferences sharedPreferences = getSharedPreferences("folderName", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("foldername", folderName);
        editor.apply();
        
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(FirebaseStorage_images.this, imageButton);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.like){ // 좋아요 누른 사진만 보이게 함
                            firebaseHandler(folderName, 3);
                        }else {
                            firebaseHandler(folderName, 4);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        // 그리드뷰 아이템 롱클릭 리스너 설정 (멀티 선택 모드 진입)
//        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
//            if (!multiSelect) {
//                multiSelect = true;
//                startSupportActionMode(actionModeCallbacks);
//                toggleSelection(position);
//            }
//            return true;
//        });

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

    // 선택된 아이템 처리
//    private void toggleSelection(int position) {
//        String selectedImage = imageUrls.get(position);
//        if (selectedImages.contains(selectedImage)) {
//            selectedImages.remove(selectedImage);
//        } else {
//            selectedImages.add(selectedImage);
//        }
//        imageAdapter.setSelectedImages(selectedImages); // 어댑터에 선택된 이미지 업데이트
//    }
//
//    // ActionMode 콜백
//    private final ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate(R.menu.popup_menu, menu);
//            imageButton.setVisibility(View.INVISIBLE);
//            return true;
//        }
//
//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }
//
//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            if (item.getItemId() == R.id.delete) {
//                // 선택된 이미지 삭제
//                imageUrls.removeAll(selectedImages);
//                //imageAdapter.notifyDataSetChanged();
//                deleteSelectedImagesFromServer(selectedImages);
//                //imageAdapter.updateData(imageUrls);
//                mode.finish();
//                return true;
//            } else if(item.getItemId() == R.id.download) {
//                Log.d(TAG, "이미지들 다운받기");
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            multiSelect = false;
//            selectedImages.clear();
//            imageAdapter.setSelectedImages(selectedImages); // 선택 해제
//            imageAdapter.notifyDataSetChanged();
//        }
//    };
//
//    // 선택된 이미지를 서버로 삭제 요청
//    private void deleteSelectedImagesFromServer(List<String> selectedImages) {
//        // Retrofit 또는 HttpURLConnection을 사용하여 서버로 삭제 요청을 보냅니다.
//        // 예: Retrofit을 사용하는 경우
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://your-server-url.com")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ImageDeleteService service = retrofit.create(ImageDeleteService.class);
//        Call<Void> call = service.deleteImages(selectedImages);
//
//        call.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    // 서버에서 삭제가 성공하면 클라이언트에서 그리드뷰를 업데이트
//                    imageUrls.removeAll(selectedImages);
//                    imageAdapter.notifyDataSetChanged();
//                } else {
//                    // 서버 오류 처리
//                    Toast.makeText(FirebaseStorage_images.this, "삭제 실패", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                // 네트워크 오류 처리
//                Toast.makeText(FirebaseStorage_images.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}