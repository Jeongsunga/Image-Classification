package com.example.picutre;
// FirebaseStorage_images에서 이미지를 하나 선택하면
// 큰 화면에 사진 한장만 보이는 화면(4번 화면)

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ImageOne extends AppCompatActivity implements ImageSliderAdapter.OnItemClickListener {

    private static final String TAG = "ImageOne";
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private int initialPosition;
    public String selectImageUrl, metadataList, oneImageUrl;
    private static final int REQUEST_WRITE_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_one);

        // Check if permissions are still granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Permissions are granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();
        }

        // 권한 체크 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        } else {
            // 권한이 이미 허용된 경우
            //downloadAndSaveImage("http://example.com/image.jpg", "example_image.jpg");
        }

        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("imagePaths"); // 이미지 URL 리스트 받기
        initialPosition = intent.getIntExtra("position", 0); // 처음 표시할 이미지의 위치
        selectImageUrl = intent.getStringExtra("selectImageUrl");

        viewPager = findViewById(R.id.viewPager);
        adapter = new ImageSliderAdapter(imageUrls, ImageOne.this, "", this);
        viewPager.setAdapter(adapter);

        // 처음 표시할 이미지 설정
        viewPager.setCurrentItem(initialPosition);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectImageUrl = imageUrls.get(position);
                String refImageUrl = extractReferencePath(selectImageUrl);
                Log.d(TAG, "참조 경로 : " + refImageUrl);

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Nullable
    public static String extractReferencePath(String url) {
        try {
            // URL에서 파일 경로 부분 추출
            String[] splitUrl = url.split("/o/");
            String pathWithParams = splitUrl[1];
            // 쿼리 파라미터 제거
            String encodedPath = pathWithParams.split("\\?")[0];
            // URL 디코딩 (ex: %2F -> /)
            return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 예외 처리
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
                //downloadAndSaveImage("http://example.com/image.jpg", "example_image.jpg");
            } else {
                //Toast.makeText(this, "Write Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onHeartClick(int position) {

    }

    @Override
    public void onInfoClick(int position) {

    }

    @Override
    public void onDownloadClick(int position) {

    }

    @Override
    public void onDeleteClick(int position) {
        //Toast.makeText(ImageOne.this, "삭제 하였습니다.22", Toast.LENGTH_SHORT).show();
        // 제대로 됨!
    }
}