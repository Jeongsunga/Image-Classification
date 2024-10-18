package com.example.picutre.ui.activity;
// 사용자가 선택한 폴더의 이미지를 파이어베이스 스토리지에 올리는 클래스(4번 화면)
// 파이어베이스 스토리지에 이미지 업로드가 완료됐을 경우 사용자에게 다이얼로그로 완료됨을 알림

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.network.interfaces.SendZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    String baseURL = BaseURL.BASE_URL;

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃 설정
            .writeTimeout(120, TimeUnit.SECONDS)   // 데이터 보내기 타임아웃 설정
            .readTimeout(300, TimeUnit.SECONDS)    // 데이터 받기 타임아웃 설정
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseURL)  // Flask 서버의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    SendZip sendZip = retrofit.create(SendZip.class);
    private static final int BUFFER_SIZE = 2048;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        Intent intent = getIntent();
        String folderPath = intent.getStringExtra("folderPath");
        //String folderName = intent.getStringExtra("foldername");
        Log.d(TAG, "폴더 경로: " + folderPath);

        // 갤러리 폴더의 경로
        File galleryFolder = new File(folderPath);
        //String serverUrl = baseURL;

        // Check if permissions are still granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
            zipAndUpload(galleryFolder);
        } else {
            Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 폴더를 압축하여 ZIP 파일로 저장하는 메서드
//    private File zipFolder(String srcFolderPath, String folderName) throws IOException {
//
//        // 내부 저장소의 기본 디렉토리 경로를 가져옴
//        File internalStorageDir = getFilesDir();
//
//        // 내부 저장소에 새로운 폴더 생성
//        File newFolder = new File(internalStorageDir, "zipFilesFolder");
//
//        if (!newFolder.exists()) {
//            boolean isCreated = newFolder.mkdirs();  // 폴더가 없으면 생성
//            if (isCreated) Log.d(TAG, "폴더 생성 성공");
//            else Log.d(TAG, "폴더 생성 실패");
//
//        } else Log.d(TAG, "이미 존재하는 폴더입니다.");
//
//        File srcFolder = new File(srcFolderPath);
//        if (!srcFolder.exists()) throw new IOException("폴더가 존재하지 않습니다: " + srcFolderPath);
//
//        File zipFile = new File(newFolder, folderName + ".zip");
//
//        // ZIP 파일이 이미 존재하는 경우 삭제
//        if (zipFile.exists()) {
//            boolean deleted = zipFile.delete();
//            if (deleted) {
//                Log.d(TAG, "기존 ZIP 파일 삭제 성공: " + zipFile.getAbsolutePath());
//            } else {
//                Log.d(TAG, "기존 ZIP 파일 삭제 실패: " + zipFile.getAbsolutePath());
//            }
//        }
//
//        // Zip 파일을 만들기 위한 스트림 열기
//        try (FileOutputStream fos = new FileOutputStream(zipFile);
//             ZipOutputStream zos = new ZipOutputStream(fos)) {
//
//            // 선택한 폴더 내의 파일들을 압축
//            zipFiles(srcFolder, srcFolder.getName(), zos);
//        }
//
//        Log.d(TAG, "압축 파일 생성: " + zipFile.getAbsolutePath());
//        return zipFile;
//    }

    // 폴더 및 파일을 재귀적으로 압축하는 메서드
    private void zipFiles(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) {
                fileName += "/";
            }
            zos.putNextEntry(new ZipEntry(fileName));
            zos.closeEntry();
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFiles(childFile, fileName + childFile.getName(), zos);
            }
            return;
        }
        // 이미지 파일인 경우
//        if (fileToZip.getName().endsWith(".jpg") || fileToZip.getName().endsWith(".png") || fileToZip.getName().endsWith(".jpeg")) {
//            // BitmapFactory.Options를 사용하여 이미지 메모리 최적화
////            BitmapFactory.Options options = new BitmapFactory.Options();
////            options.inSampleSize = 4; // 1/4 크기로 샘플링
//
//            // 비트맵을 로드
//            Bitmap bitmap = BitmapFactory.decodeFile(fileToZip.getAbsolutePath(), options);
//            // 비트맵을 파일로 다시 저장하거나 처리하는 로직 추가
//
//            // 비트맵을 사용한 후 반드시 recycle() 호출
//            if (bitmap != null) {
//                bitmap.recycle();
//            }
//        }
    }

    public void zipAndUpload(File folder) {
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
                    // 이미지 파일인 경우 비트맵으로 변환 후 압축
                    //BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 3; // 1/3 크기로 샘플링

                    // 비트맵을 메모리에서 읽어오기
//                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath()/*, options*/);
//
//                    if (bitmap != null) {
//                        // 비트맵을 ByteArrayOutputStream으로 압축
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos); // JPEG 포맷으로 압축
//
//                        // 압축된 바이트 배열을 ZIP에 추가
//                        byte[] buffer = bos.toByteArray();
//                        String zipEntryName = parentFolder + "/" + file.getName();
//                        zos.putNextEntry(new ZipEntry(zipEntryName));
//                        zos.write(buffer);
//                        zos.closeEntry();
//
//                        // 비트맵 메모리 해제
//                        bitmap.recycle();
//                    } else {
//                        Log.d(TAG, "비트맵 생성 실패: " + file.getAbsolutePath());
//                    }
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
        //Toast.makeText(LoadingScreen.this, "폴더 압축 완료", Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
                    builder.setMessage("분류 과정에 문제가 생겨 중단되었습니다.");
                    builder.setIcon(R.drawable.ic_launcher_foreground);
                    builder.setCancelable(false); // 뒤로가기 버튼으로 다이얼로그 종료 못함
                    builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(LoadingScreen.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("ZipUpload", "Error123123: " + t.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
                builder.setMessage("분류 과정에 문제가 생겨 중단되었습니다.");
                builder.setIcon(R.drawable.ic_launcher_foreground);
                builder.setCancelable(false); // 뒤로가기 버튼으로 다이얼로그 종료 못함
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(LoadingScreen.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}