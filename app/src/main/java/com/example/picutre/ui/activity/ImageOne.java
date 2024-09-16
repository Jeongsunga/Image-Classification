package com.example.picutre.ui.activity;
// FirebaseStorage_images에서 이미지를 하나 선택하면
// 큰 화면에 사진 한장만 보이는 화면(4번 화면)


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.DeleteResponse;
import com.example.picutre.model.LinkAndHeart;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.adapter.ImageSliderAdapter;
import com.example.picutre.R;
import com.example.picutre.network.interfaces.DeleteImageApi;
import com.example.picutre.network.interfaces.DownloadImage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ImageOne extends AppCompatActivity implements ImageSliderAdapter.OnItemClickListener {

    private static final String TAG = "ImageOne";
    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<String> imageUrls, imageLinks;
    private int initialPosition, imageCount;
    public String selectImageUrl;
    private DownloadImage downloadImage;
    private DeleteImageApi deleteImageApi;
    private boolean success;
    String baseurl = BaseURL.BASE_URL;
    Retrofit retrofit = RetrofitClient.getClient(baseurl);
    private LinkAndHeart linkAndHeart = new LinkAndHeart();


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
        Log.d(TAG, "해시 처리 하기 전에 이미지 링크: " + selectImageUrl);

        // 파이어베이스 리얼타임 스토리지에는 특수문자가 저장되지 않아, 이미지 링크를 해시값으로 변환해 저장
        // MD5는 동일한 입력값이면 출력값도 동일하기 때문에 스토리지에서 찾을 수 있음.
        String urlToHash = getHash(selectImageUrl);
        Log.d(TAG, "해시 처리 후의 링크: " + urlToHash);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                    Log.d(TAG, "파이어베이스에 데이터가 없습니다.");
                    DatabaseReference key = database.getReference(urlToHash);
                    key.setValue(false);
                    Toast.makeText(ImageOne.this, "DB에 저장되었습니다.1", Toast.LENGTH_SHORT).show();
                    linkAndHeart.setHeart(false);
                }
                else {
                    boolean imageExists = false;  // 데이터 존재 여부를 추적하기 위한 변수

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {  //반복문으로 데이터 리스트 추출

                        if(snapshot.getKey().equals(urlToHash)) {
                            imageExists = true;  // 데이터가 존재하는 경우

                            if(snapshot.getValue(Boolean.class)) {
                                Log.d(TAG, "해당 이미지가 존재하며, true입니다.");
                                linkAndHeart.setHeart(true);
                                break;
                            }
                            else {
                                Log.d(TAG, "해당 이미지가 존재하며, false입니다.");
                                linkAndHeart.setHeart(false);
                                break;
                            }
                        }
                    }

                    // 반복문이 끝난 후 데이터 존재 여부 확인
                    if (!imageExists) {
                        Log.d(TAG, "해당 이미지가 존재하지 않습니다.");
                        DatabaseReference key = database.getReference().child(urlToHash);
                        key.setValue(false);
                        Toast.makeText(ImageOne.this, "DB에 저장되었습니다.2", Toast.LENGTH_SHORT).show();
                        linkAndHeart.setHeart(false);
                    }
                }
                linkAndHeart.setImageUrl(urlToHash);
                Log.d(TAG, "파이어베이스 링크: " + linkAndHeart.getImageUrl() + " 불린 값: " + linkAndHeart.isHeart());

                // 여기에 다음에 수행될 동작들을 넣음
                viewPager = findViewById(R.id.viewPager);
                adapter = new ImageSliderAdapter(imageUrls, ImageOne.this, linkAndHeart, ImageOne.this);
                viewPager.setAdapter(adapter);

                // 처음 표시할 이미지 설정
                viewPager.setCurrentItem(initialPosition);

                viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        selectImageUrl = imageUrls.get(position);
                        String hashedUrl = getHash(selectImageUrl);
                        //String refImageUrl = extractReferencePath(selectImageUrl);
                        //Log.d(TAG, "참조 경로 : " + refImageUrl);

                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child(hashedUrl);
                        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(dataSnapshot.exists()) { // 파이어베이스 내에 데이터가 하나라도 있을 때
                                    Object data = dataSnapshot.getValue();
                                    if(data != null && data instanceof HashMap) {
                                        HashMap<String, Object> map = (HashMap<String, Object>) data;
                                        Boolean heartState = (Boolean) map.get(hashedUrl);
                                        if(heartState != null) {
                                            if(heartState) { // 이미지의 좋아요값이 true 일 때
                                                linkAndHeart.setHeart(heartState);
                                                adapter.notifyItemChanged(position);
                                            }else{ // 이미지의 좋아요값이 false 일 때
                                                linkAndHeart.setHeart(false); // 기본값
                                                adapter.notifyItemChanged(position);
                                            }
                                        }else {  // 이미지의 해시값이 없을 때
                                            DatabaseReference key = database.getReference(hashedUrl);
                                            key.setValue(false);
                                            Log.d(TAG, "이미지의 해시 값 존재 X, DB에 업로드 완료");
                                            linkAndHeart.setHeart(false); // 기본값
                                            adapter.notifyItemChanged(position);
                                        }
                                    }
                                }else { // 파이어베이스에 데이터가 아예 없을 때, 데이터베이스에 새로 올리는 코드 작성
                                    DatabaseReference key = database.getReference(hashedUrl);
                                    key.setValue(false);
                                    Log.d(TAG, "DB에 아무런 데이터가 없음, DB에 업로드 완료");
                                    linkAndHeart.setHeart(false); // 기본값
                                    adapter.notifyItemChanged(position);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "Firebase에서 데이터를 가져오지 못했습니다.");
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "파이어베이스 데이터베이스 에러");
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*@Nullable
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
    }*/

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
        showDeleteDialog(selectImageUrl);
    }

    public void showDownloadDialog(String imageUrl) {
        new AlertDialog.Builder(ImageOne.this)
                .setTitle("다운로드")
                .setMessage("해당 사진을 갤러리에 저장하시겠습니까?")
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
                            Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                            String folderName = getFolderNameFromUrl(imagePath);

                            // 외부 저장소 경로 설정 (폴더 생성)
                            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "저장되었습니다.", Toast.LENGTH_SHORT).show());

                        } catch (IOException e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show());
                            Log.d(TAG, "예외 발생");
                        }
                    }).start();
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show());
                    Log.d(TAG, "응답이 실패하였거나 바디가 비었습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ImageOne.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show());
                Log.d(TAG, "응답이 오지 않았습니다.");
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

    public void showDeleteDialog(String imageUrl) {
        new AlertDialog.Builder(ImageOne.this)
                .setTitle("삭제")
                .setMessage("해당 사진을 서버에서 삭제하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 서버에 있는 이미지를 삭제하는 동작 수행
                        sendDeleteImageUrl(imageUrl);
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 아무 동작 수행 X
                    }
                }).show();
    }

    public void sendDeleteImageUrl(String imageUrl) {
        deleteImageApi = retrofit.create(DeleteImageApi.class);
        Call<DeleteResponse> call = deleteImageApi.sendUrl(imageUrl);

        call.enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if(response.isSuccessful() && response.body() != null) {
                    DeleteResponse deleteResponse = response.body();

                    success = deleteResponse.isSuccess();
                    imageCount = deleteResponse.getImageCount();
                    imageLinks = deleteResponse.getImageLinks();
                    Log.d(TAG, "성공여부: " + success + " 삭제 후 폴더 내의 사진 장수: " + imageCount + " 이미지 링크 리스트: " + imageLinks);

                    // 업데이트 된 정보를 폰에도 동기화해야 함(사진 장수, 이미지)
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("imageCount", imageCount);
                    resultIntent.putStringArrayListExtra("imageLinks", (ArrayList<String>)imageLinks);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // 이전 화면으로 돌아감(FirebaseStorage_images Activity)
                }else {
                    Toast.makeText(ImageOne.this, "응답 바디가 비었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                Toast.makeText(ImageOne.this, "응답이 오지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 이미지 URL을 해시로 변환 (MD5 사용 예시)
    public String getHash(@NonNull String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}