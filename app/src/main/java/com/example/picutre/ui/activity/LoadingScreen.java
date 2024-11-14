package com.example.picutre.ui.activity;
// 사용자가 선택한 폴더의 이미지를 파이어베이스 스토리지에 올리는 클래스(4번 화면)
// 파이어베이스 스토리지에 이미지 업로드가 완료됐을 경우 사용자에게 다이얼로그로 완료됨을 알림

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;

import com.example.picutre.model.FileRequestBody;
import com.example.picutre.network.interfaces.NewZipApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoadingScreen extends AppCompatActivity {

    String baseURL = BaseURL.BASE_URL;

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // 연결 타임아웃 설정
            .writeTimeout(300, TimeUnit.SECONDS)   // 데이터 보내기 타임아웃 설정
            .readTimeout(360, TimeUnit.SECONDS)    // 데이터 받기 타임아웃 설정
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseURL)  // Flask 서버의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    NewZipApi newZipApi = retrofit.create(NewZipApi.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        Intent intent = getIntent();
        String folderName = intent.getStringExtra("folderName");

        // Check if permissions are still granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // PICTURES & DCIM 폴더 안에 사용자가 선택한 폴더가 있는지 검사
            File galleryFolder = findGalleryFolder(folderName);

            // 스마트폰의 다운로드 디렉터리에 새 폴더 생성
            File downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File newFolder = new File(downloadFolder, "ZIPFolder");

            // 폴더가 존재하지 않으면 생성
            if (!newFolder.exists()) {
                boolean folderCreated = newFolder.mkdirs();
                if (folderCreated) Log.d(TAG, "폴더 생성 완료");
                else Log.d(TAG, "폴더 생성 실패");
            } else Log.d(TAG, "폴더 이미 존재");

            // 앱의 다운로드 디렉터리에 압축 파일 생성
            if (galleryFolder != null && galleryFolder.exists()) {
                File zipFile = new File(newFolder, folderName + ".zip");

                // 폴더를 압축
                try {
                    zipFolder(galleryFolder, zipFile.getAbsolutePath());
                    uploadFile(zipFile);
                } catch (IOException e) {
                    Log.d(TAG, "압축 에러 " + e);
                }
            } else Toast.makeText(LoadingScreen.this, "압축 파일 생성 실패", Toast.LENGTH_SHORT).show();

        } else Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private File findGalleryFolder(String folderName) {
        // Pictures 폴더 경로
        File picturesFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);

        // DCIM 폴더 경로
        File dcimFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), folderName);

        // 존재하는 폴더 반환
        if (picturesFolder.exists()) return picturesFolder;
        else if (dcimFolder.exists()) return dcimFolder;
        else return null;
    }

    public void zipFolder(File folder, String zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
                zipFiles(folder, folder, zos);
        }
    }

    private void zipFiles(File rootFolder, File sourceFile, ZipOutputStream zos) throws IOException {
        if (sourceFile.isDirectory()) {
            for (File file : sourceFile.listFiles()) {
                zipFiles(rootFolder, file, zos);
            }
        } else {
            String zipEntryName = sourceFile.getAbsolutePath().substring(rootFolder.getAbsolutePath().length() + 1);
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                zos.putNextEntry(new ZipEntry(zipEntryName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    private void showDialogAutomatically() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
        builder.setTitle("분류 완료")
                .setMessage("분류 결과를 확인 하시겠습니까?");
        builder.setCancelable(false); // 뒤로가기 버튼으로 다이얼로그 종료 못함
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(LoadingScreen.this, InAppGallery.class);
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
        }).create().show();
    }

    public void uploadFile(File file) {
        FileRequestBody fileRequestBody = new FileRequestBody(file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), fileRequestBody);

        Call<ResponseBody> call = newZipApi.uploadFile(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) showDialogAutomatically();
                 else {
                    Toast.makeText(LoadingScreen.this, "분류 실패", Toast.LENGTH_SHORT).show();
                    try {
                        Log.d(TAG, "응답 실패 " + response.errorBody().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoadingScreen.this, "네트워크 에러", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "에러 이유: " + t.getMessage());
                finish();
            }
        });
    }
}