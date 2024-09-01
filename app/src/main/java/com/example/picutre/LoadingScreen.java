package com.example.picutre;
// 사용자가 선택한 폴더의 이미지를 파이어베이스 스토리지에 올리는 클래스(4번 화면)
// 파이어베이스 스토리지에 이미지 업로드가 완료됐을 경우 사용자에게 다이얼로그로 완료됨을 알림

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoadingScreen extends AppCompatActivity {

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://172.21.195.40:5000")  // Flask 서버의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    SendZip sendZip = retrofit.create(SendZip.class);;

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
        String serverUrl = "http://172.21.195.40:5000";

        // 폴더 압축 및 업로드
        // 폴더 내의 데이터가 크면 처리하는데 시간이 걸림
        zipAndUpload(galleryFolder, serverUrl);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showDialogAutomatically() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoadingScreen.this);
        //builder.setTitle("권한 허");
        builder.setMessage("분류가 완료되었습니다. 분류 결과를 확인 하시겠습니까?");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setCancelable(false); // 뒤로가기 버튼으로 다이얼로그 종료 못함
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(LoadingScreen.this, inAppGallery.class);
                startActivity(intent);
                // 실제 서버에서 분류하는데 시간이 걸리므로 바로 들어가면 확인 불가
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

    public void zipAndUpload(File folder, String serverUrl) {
        try {
            // 이미지에서 GPS 정보 추출
            JSONArray gpsJSON = extractGPS(LoadingScreen.this, (folder.toString()));
            
            // 메모리에서 ZIP 파일 생성
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            zipDirectoryToStream(folder, folder.getName(), zipOutputStream);
            zipOutputStream.close();

            byte[] zipData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // JSON 데이터를 바이트 배열로 변환
            String jsonString = gpsJSON.toString();
            byte[] jsonData = jsonString.getBytes("UTF-8");

            // 서버로 ZIP 파일과 json 데이터 전송
            uploadFiles(zipData, folder.getName() + ".zip");

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // ZIP 파일 RequestBody
        RequestBody zipRequestBody = RequestBody.create(MediaType.parse("application/zip"), zipData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("zip_file", fileName, zipRequestBody);
        //Log.d(TAG, "fileName : " + fileName);

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

    public JSONArray extractGPS(Context context, String folderPath) {
        JSONArray imagesArray = new JSONArray();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE
        };
        String selection = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{folderPath + "%"};
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE));
                    @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE));

                    JSONObject imageObject = new JSONObject();
                    imageObject.put("file_name", filePath.substring(filePath.lastIndexOf("/") + 1));
                    imageObject.put("latitude", latitude);
                    imageObject.put("longitude", longitude);

                    imagesArray.put(imageObject);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse image data", e);
            } finally {
                cursor.close();
            }
        }
        return imagesArray;
    }
}
