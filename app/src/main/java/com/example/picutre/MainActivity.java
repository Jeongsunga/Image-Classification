package com.example.picutre;
// 앱을 처음 켜면 보이는 1번 화면
// 사진 분류하기 & DB 내의 분류 결과를 확인하는 버튼 2가지가 있다.

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_ALLOWED = "isAllowed";

    private Button btn_sort;
    private Button btn_inappGallery;

    private long backBtnTime = 0;
    //final int GET_GALLERY_IMAGE = 200;
    private int permission = 0;

    //뒤로가기 버튼을 두 번 눌러야 어플 종료
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isAllowed = preferences.getBoolean(PREF_ALLOWED, false);

        btn_sort = findViewById(R.id.btn_sort);
        btn_inappGallery = findViewById(R.id.btn_inappGallery);

        if (isAllowed) {
            // 권한이 이미 허용된 경우, 메인 콘텐츠를 로드
            showPermissionDialog();  //실행이 잘 되는지만 확인하기 위해 기입해놓은 코드, 잘 동작됨을 확인한 이후에는 지울 예정
        } else {
            // 권한을 요청합니다.
            //showDialogAutomatically(); // 다이얼로그 자동으로 띄우는 메소드
            showPermissionDialog();
        }

        btn_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Filter.class);
                startActivity(intent);
            }
        });

        // 어플 내 갤러리 버튼을 눌렀을 때 실행되는 동작
        btn_inappGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, inAppGallery.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("미디어 접근 권한 요청")
                .setMessage("앱에서 미디어에 접근하려면 권한이 필요합니다. 권한을 허용하시겠습니까?")
                .setPositiveButton("허용", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestMediaPermissions(); // 미디어 접근 허용하도록 하기
                    }
                })
                .setNegativeButton("거절", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "권한이 거절되었습니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean permissionGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }
            if (permissionGranted) {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREF_ALLOWED, true);
                editor.apply();
            }else {
                Toast.makeText(this, "권한이 거절되었습니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
