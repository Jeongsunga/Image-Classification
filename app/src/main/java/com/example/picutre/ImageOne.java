package com.example.picutre;
// FirebaseStorage_images에서 이미지를 하나 선택하면
// 큰 화면에 사진 한장만 보이는 화면(4번 화면)

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.List;

public class ImageOne extends AppCompatActivity {

    private static final String TAG = "ImageOne";
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private int initialPosition;
    public String selectImageUrl, metadataList, oneImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_one);

        Intent intent = getIntent();
        imageUrls = intent.getStringArrayListExtra("imageUrls"); // 이미지 URL 리스트 받기
        initialPosition = intent.getIntExtra("position", 0); // 처음 표시할 이미지의 위치
        selectImageUrl = intent.getStringExtra("selectImageUrl");

        // Firebase Storage 인스턴스 초기화
        FirebaseStorage storage = FirebaseStorage.getInstance();

        //String refImageUrl = extractReferencePath(selectImageUrl);
        //Log.d(TAG, "참조 경로 : " + refImageUrl);
        // 스토리지에서 삭제할 파일의 참조를 가져옵니다. 파일 경로는 Firebase Storage의 참조경로를 사용합니다.
        //StorageReference storageRef = storage.getReference().child(refImageUrl);

        viewPager = findViewById(R.id.viewPager);
        adapter = new ImageSliderAdapter(imageUrls, ImageOne.this, ""/*, ""*/);
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
                StorageReference storageRef = storage.getReference().child(refImageUrl);

                storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata metadata) {
                        // 메타데이터가 성공적으로 가져온 경우
                        String name = metadata.getName(); // 파일 이름
                        String path = metadata.getPath(); // 파일 경로
                        //String contentType = metadata.getContentType(); // 콘텐츠 타입

                        long sizeBytes = metadata.getSizeBytes(); // 파일 크기 (바이트 단위)
                        String volume = formatFileSize(sizeBytes); // 사용자가 읽기 편한 단위로 변환

                        String dateTaken = metadata.getCustomMetadata("DateTime");  // 사진 촬영 날짜
                        String cameraModel = metadata.getCustomMetadata("CameraModel"); // 사진 촬영 카메라 모델

                        String flashMode = metadata.getCustomMetadata("FlashMode"); // 사진 촬영시 플래시 코드
                        if(flashMode.equals("0")) flashMode = "플래시 끔";
                        else flashMode = "플래시 켬";

                        String manufacturer = metadata.getCustomMetadata("Manufacturer"); // 카메라 제조사
                        String gps = metadata.getCustomMetadata("Gps"); // 사진 촬영 위치

                        // GPS값만 null이 뜸
                        Log.d(TAG, "imageGPS : " + gps);

                        metadataList = "폴더/파일 이름 : " + path + "\n사진 크기 : " + volume +
                                "\n\n촬영 시간 : " + dateTaken + "\n카메라 제조사 : " + manufacturer + "\n카메라 모델 : " + cameraModel
                                + "\n플래시 모드 : " + flashMode;

                        adapter.updateMetadata(metadataList);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // 메타데이터 가져오기 실패 시
                        Log.e(TAG, "Error getting metadata", exception);
                    }
                });

                //adapter.updateRefImageUrl(refImageUrl);
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

    @NonNull
    public static String formatFileSize(long bytes) {
        if (bytes >= 1073741824) { // 1 GB = 2^30 bytes
            return String.format("%.2f GB", bytes / 1073741824.0);
        } else if (bytes >= 1048576) { // 1 MB = 2^20 bytes
            return String.format("%.2f MB", bytes / 1048576.0);
        } else if (bytes >= 1024) { // 1 KB = 2^10 bytes
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%d bytes", bytes);
        }
    }

}