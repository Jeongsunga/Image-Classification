package com.example.picutre;
// FirebaseStorage_images에서 이미지를 하나 선택하면
// 큰 화면에 사진 한장만 보이는 화면(4번 화면)

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageOne extends AppCompatActivity implements ImageSliderAdapter.OnItemClickListener {

    private static final String TAG = "ImageOne";
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls;
    private int initialPosition;
    public String selectImageUrl;
    private static final int REQUEST_WRITE_STORAGE = 1;
    private DownloadImage downloadImage;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://172.21.223.102:5000/")  // 로컬 호스트 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_one);

        // Check if permissions are still granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();
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
    public void onHeartClick(int position) {

    }

    @Override
    public void onInfoClick(int position) {

    }

    @Override
    public void onDownloadClick(int position) {
        showDownloadDialog(selectImageUrl);
    }

    @Override
    public void onDeleteClick(int position) {

    }

    public void showDownloadDialog(String imageUrl) {
        new AlertDialog.Builder(ImageOne.this)
                .setTitle("다운로드")
                .setMessage("이 사진을 갤러리에 저장하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAndSaveImage(imageUrl); // 서버의 이미지 URL 경로
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 아무 동작 수행 X
                    }
                }).show();
    }

    private void downloadAndSaveImage(String imagePath) {
        Log.d(TAG, "imageUrl: " + imagePath);
        downloadImage = retrofit.create(DownloadImage.class);
        Call<ResponseBody> call = downloadImage.downloadImage(imagePath);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            // 서버에서 이미지 다운로드
                            ResponseBody body = response.body();
                            //Log.d(TAG, "ResponseBody: " + body);
                            //InputStream inputStream = body.byteStream(); // Initialize InputStream
                            //Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());

                            // EXIF 정보를 읽기 위해 InputStream을 재설정
                            //inputStream.close(); // 이전 InputStream을 닫기

                            // 이미지 URL에서 폴더 이름 추출 (예: "서울_test1")
                            String folderName = getFolderNameFromUrl(imagePath);

                            // 외부 저장소에 이미지 저장 -> Pictures 라는 공동 파일에 저장됨
//                            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                            File file = new File(externalStorageDir, "downloaded_image.jpg");
//
//                            try (OutputStream output = new FileOutputStream(file)) {
//                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
//                            }
//
//                            // 미디어 스캐너에 이미지 추가 알림
//                            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), null);
//
//                            // 갤러리 업데이트
//                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                            // 외부 저장소 경로 설정 (폴더 생성)
                            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            //File externalStorageDir = getExternalFilesDir(null); // Use app-specific directory
                            File folder = new File(externalStorageDir, folderName);
                            if (!folder.exists()) {
                                folder.mkdirs(); // 폴더가 없으면 생성
                            }

                            // 이미지 파일 경로 설정
                            String imageName = getImageNameFromUrl(imagePath);
                            File file = new File(folder, imageName);

                            // 이미지를 저장
                            try (OutputStream output = new FileOutputStream(file)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                            }

                            // 미디어 스캐너에 이미지 추가 알림
                            //MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), null);

                            // 갤러리 업데이트
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                            // UI 스레드에서 Toast 메시지
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "Image saved to gallery", Toast.LENGTH_SHORT).show());

                        } catch (IOException e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "Failed to save image", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "Failed to download image1", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "Failed to download image2", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 이미지 URL에서 폴더 이름 추출 메서드
    private String getFolderNameFromUrl(String url) {
        // 예시 URL: http://172.21.223.102:5000/images/서울_test1/20240413_152945.jpg
        Uri uri = Uri.parse(url);
        List<String> pathSegments = uri.getPathSegments();
        // "서울_test1" 부분을 추출 (images 뒤에 있는 폴더명)
        return pathSegments.get(pathSegments.size() - 2);
    }

    // 이미지 URL에서 파일 이름 추출 메서드
    private String getImageNameFromUrl(String url) {
        // 예시 URL: http://172.21.223.102:5000/images/서울_test1/20240413_152945.jpg
        Uri uri = Uri.parse(url);
        return uri.getLastPathSegment(); // "20240413_152945.jpg" 부분을 추출
    }
}