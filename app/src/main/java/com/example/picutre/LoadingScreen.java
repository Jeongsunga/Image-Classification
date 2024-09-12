package com.example.picutre;
// 사용자가 선택한 폴더의 이미지를 파이어베이스 스토리지에 올리는 클래스(4번 화면)
// 파이어베이스 스토리지에 이미지 업로드가 완료됐을 경우 사용자에게 다이얼로그로 완료됨을 알림

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoadingScreen extends AppCompatActivity {

    String baseURL = "http://172.21.223.102:5000";

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 설정
            .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃 설정
            .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 설정
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseURL)  // Flask 서버의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    SendZip sendZip = retrofit.create(SendZip.class);
    private static final int BUFFER_SIZE = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        Intent intent = getIntent();
        String folderPath = intent.getStringExtra("folderPath");

        // 갤러리 폴더의 경로
        File galleryFolder = new File(folderPath);
        String serverUrl = baseURL;

        // Check if permissions are still granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permissions are granted, proceed with your logic
            //Toast.makeText(this, "Permissions are granted here too", Toast.LENGTH_SHORT).show();
            zipAndUpload(galleryFolder, serverUrl);
        } else {
            // Handle the case where permissions are not granted
            Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void zipAndUpload(File folder, String serverUrl) {
        try {
            // 메모리에서 ZIP 파일 생성
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            zipDirectoryToStream(folder, folder.getName(), zipOutputStream);
            zipOutputStream.close();

            byte[] zipData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            uploadFiles(zipData, folder.getName() + ".zip");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialogAutomatically() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
        builder.setMessage("분류가 완료되었습니다. 분류 결과를 확인 하시겠습니까?");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(false); // 뒤로가기 버튼으로 다이얼로그 종료 못함
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(LoadingScreen.this, inAppGallery.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(LoadingScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        builder.show();
    }

    private void zipDirectoryToStream(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDirectoryToStream(file, parentFolder + "/" + file.getName(), zos);
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    String zipEntryName = parentFolder + "/" + file.getName();
                    zos.putNextEntry(new ZipEntry(zipEntryName));

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    fis.close();
                }
            }
        }
    }

    private void uploadFiles(byte[] zipData, String fileName) {
        RequestBody zipRequestBody = RequestBody.create(MediaType.parse("application/zip"), zipData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", fileName, zipRequestBody);

        Call<ResponseBody> call = sendZip.uploadZipFile(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    showDialogAutomatically(); // 완료 되었다고 결과를 확인하겠냐고 묻는 다이얼로그
                    Log.d("ZipUpload", "File uploaded successfully123123 : " + response.message()); // 200
                } else {
                    Log.d("ZipUpload", "File upload failed123123: " + response.message());
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ZipUpload", "Error123123: " + t.getMessage());
            }
        });
    }
}