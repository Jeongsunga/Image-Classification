package com.example.picutre;
// 사용자가 선택한 폴더의 이미지를 파이어베이스 스토리지에 올리는 클래스(4번 화면)
// 파이어베이스 스토리지에 이미지 업로드가 완료됐을 경우 사용자에게 다이얼로그로 완료됨을 알림

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.Button;
import android.Manifest;
import android.content.pm.PackageManager;
import android.webkit.MimeTypeMap;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoadingScreen extends AppCompatActivity {

    private Button btn_next, btn_test;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "LoadingScreen";
    private String folderPath;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_screen);

        btn_next = findViewById(R.id.btn_next);
        btn_test = findViewById(R.id.btn_test);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //권한 요청 코드
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "권한요청");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        folderPath = getIntent().getStringExtra("folderPath");
        if (folderPath != null) {
            uploadImages(folderPath);
            //String dateTime = takeMetadata(folderPath);
            //Log.d(TAG, "dateTime : "+dateTime);

        }

        showDialogAutomatically(); // 다이얼로그 자동으로 띄우는 메소드

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인됨, 이미지 업로드 실행
            } else {
                //Toast.makeText(this, "외부 저장소 읽기 권한이 거부되었습니다", Toast.LENGTH_SHORT).show();
                //showPermissionDialog();
                requestMediaPermissions(); // 미디어 접근 허용하도록 하기
                //Log.d(TAG, "권한 허용되었습니다.");
            }

            folderPath = getIntent().getStringExtra("folderPath");
            if (folderPath != null) {
                Log.d(TAG, "업로드 이미지");
                uploadImages(folderPath);
            }

        }
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
                // 실제 파이어베이스 스토리지에서 결과를 반영하고 휴대폰으로 결과를 확인하는데 시간이 걸리므로
                // 바로 들어가면 확인할 수 없음
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

    private void requestMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,

            }, PERMISSION_REQUEST_CODE);
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        }
    }

    private void uploadImages(String folderPath) {
        File folder = new File(folderPath);


        // 디렉터리 존재 여부 확인
        if (!folder.exists() || !folder.isDirectory()) {
            Log.e(TAG, "디렉터리가 존재하지 않거나 디렉터리가 아닙니다: " + folderPath);
            return;
        }
        File[] files = folder.listFiles();

        if (files != null) { //파일이 널이 아니면 아래 실행
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {

                    uploadImageToFirebase(this, file);
                }
            }
        }else {
            Log.e(TAG, "디렉터리에 파일이 없습니다: " + folderPath);
        }
    }

    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {"jpg", "jpeg", "png"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
    
    private void uploadImageToFirebase(Context context, File file) {

        Uri fileUri = Uri.fromFile(file);
        String datatime = null;
        String cameraModel = null;
        String flashMode = null;
        String manufacturer = null;
        String owner = null;
        String gps = null;
//        long fileSizeInBytes = file.length();
//        String fileSizeString = formatFileSize(fileSizeInBytes);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            ExifInterface exif = new ExifInterface(inputStream);

            datatime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL);
            flashMode = exif.getAttribute(ExifInterface.TAG_FLASH);
            manufacturer = exif.getAttribute(ExifInterface.TAG_MAKE);
            owner = exif.getAttribute(ExifInterface.TAG_ARTIST);
            gps = exif.getAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION);

        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException 발생", e);
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/" + extension) // 이미지의 MIME 타입 설정
                .setCustomMetadata("DateTime", datatime) // 사용자 정의 메타데이터 추가
                .setCustomMetadata("CameraModel", cameraModel)
                .setCustomMetadata("FlashMode", flashMode)
                .setCustomMetadata("Manufacturer", manufacturer)
                .setCustomMetadata("GPS", gps)
                .build();

        String folderName = file.getParent().toString();
        int lastSlashIndex = folderName.lastIndexOf('/');
        // 마지막 슬래시 다음 문자열을 추출한다 -> 파이어베이스 스토리지 저장 폴더명
        String lastSegment = folderName.substring(lastSlashIndex + 1);

        //StorageReference fileReference = storageReference.child("images/" + file.getName());
        Log.d(TAG, "파이어베이스 저장 폴더 이름 : " + lastSegment);
        StorageReference fileReference = storageReference.child(lastSegment + "/" + file.getName());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");

        // 파이어베이스 스토리지에 파일을 올리는 코드, 각 이미지 파일 경로가 나옴
        UploadTask uploadTask = fileReference.putFile(fileUri, metadata);
        Log.d(TAG, "File URL : " + fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            //Log.d(TAG, "Image uploaded successfully: " + file.getName());
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            //Log.e(TAG, "Failed to upload image: " + file.getName(), e);
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
        });


    }

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

