package com.example.picutre;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.util.Log;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZipUpload {
    
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.7.10:5000")  // Flask 서버의 기본 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    SendZip sendZip = retrofit.create(SendZip.class);;

    private static final int BUFFER_SIZE = 2048;

    public void zipAndUpload(File folder, String serverUrl) {
        try {
            // 메모리에서 ZIP 파일 생성
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            zipDirectoryToStream(folder, folder.getName(), zipOutputStream);
            zipOutputStream.close();

            byte[] zipData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            // 서버로 ZIP 데이터 전송
            uploadZipData(zipData, folder.getName() + ".zip");

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

    private void uploadZipData(byte[] zipData, String fileName) {
        // Retrofit을 통해 ZIP 파일 업로드
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), zipData);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", fileName, requestFile);
        Log.d(TAG, "fileName : " + fileName);

        Call<ResponseBody> call = sendZip.uploadZipFile(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // LoadingScreen으로 넘겨줘야함..
                    Log.d("ZipUpload", "File uploaded successfully123123 : " + response.message());
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