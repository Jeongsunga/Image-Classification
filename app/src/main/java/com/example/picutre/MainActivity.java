package com.example.picutre;
// 앱을 처음 켜면 보이는 1번 화면
// 사진 분류하기 & DB 내의 분류 결과를 확인하는 버튼 2가지가 있다.


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button btn_sort;
    private Button btn_inappGallery;
    private long backBtnTime = 0;
    private ApiService apiService;

    //뒤로가기 버튼을 두 번 눌러야 어플 종료
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://172.21.249.56:5000/")  // 로컬 호스트 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Check if the permissions are granted
        if (!checkPermissions()) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.ACCESS_MEDIA_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

        btn_sort = findViewById(R.id.btn_sort);
        btn_inappGallery = findViewById(R.id.btn_inappGallery);

        btn_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToServer(); //앱과 서버가 연결되어 있는지 확인할 수 있는 간단한 코드
                Intent intent = new Intent(MainActivity.this, Filter.class);
                startActivity(intent);
            }
        });

        // 어플 내 갤러리 버튼을 눌렀을 때 실행되는 동작
        btn_inappGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToServer(); //앱과 서버가 연결되어 있는지 확인할 수 있는 간단한 코드
                Intent intent = new Intent(MainActivity.this, inAppGallery.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                //Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permissions denied
                //Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendDataToServer() {
        // 요청 데이터 생성
        DataModel request = new DataModel("HoJeong & SeungA", 24);

        // 서버로 요청 보내기
        Call<ResponseData> call = apiService.sendData(request);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    ResponseData myResponse = response.body();
                    Log.d("MainActivity", "Success123456: " + myResponse.getMessage());
                } else {
                    Log.d("MainActivity", "Request failed123456: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e("MainActivity", "Error123456: " + t.getMessage());
            }
        });
    }

}
