package com.example.picutre;
// 사용자가 분류하고자 하는 방식을 정하는 화면(2번)

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Filter extends AppCompatActivity {

    Button btn_next;
    CheckBox chbox_locate;
    CheckBox chbox_eyeclosed;
    CheckBox chbox_faceOpen;
    CheckBox chbox_hopeDate;

    //RadioButton radiobtn_all;

    private static final int REQUEST_GALLERY = 1001;
    private static final int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);

        btn_next = findViewById(R.id.btn_next);
        chbox_locate = findViewById(R.id.chbox_locate);
        chbox_eyeclosed = findViewById(R.id.chbox_eyeclosed);
        chbox_faceOpen = findViewById(R.id.chbox_faceOpen);
        chbox_hopeDate = findViewById(R.id.chbox_hopeDate);
        //radiobtn_all = findViewById(R.id.radiobtn_all);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //분류 필터를 한 가지도 선택하지 않았을 때 토스트 문구 알림
                if(chbox_faceOpen.isChecked() == false && chbox_eyeclosed.isChecked() == false &&
                        chbox_hopeDate.isChecked() == false && chbox_locate.isChecked() == false) {
                    Toast toast = Toast.makeText(getApplicationContext(), "필터를 한 가지 선택해 주세요.",Toast.LENGTH_SHORT);
                    toast.show();
                }
                /* 
                선택할 수 있는 필터 
                1. 얼굴 보이기
                2. 얼굴 보이기 & 눈 뜨기
                3. 날짜
                4. 위치

                사용자가 선택한 분류 방식에 따라 서버에 다른 값을 넘겨주어 분류 파이썬 코드를 돌아가게 한다.
                 */
                if(chbox_faceOpen.isChecked() == true && (chbox_locate.isChecked() == true || chbox_hopeDate.isChecked() == true)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT);
                    toast.show();
                }else if(chbox_hopeDate.isChecked() == true && (chbox_faceOpen.isChecked() == true || chbox_locate.isChecked()==true)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT);
                    toast.show();
                }else if(chbox_locate.isChecked()==true && (chbox_faceOpen.isChecked()==true || chbox_hopeDate.isChecked()==true)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT);
                    toast.show();
                }
                //얼굴 보이기 or (얼굴 보이기 & 눈 뜨기) 필터만 선택되었을 때
                if(chbox_faceOpen.isChecked() == true && chbox_hopeDate.isChecked() == false && chbox_locate.isChecked() == false) {
                    Intent intent = new Intent(Filter.this, GalleryList.class);
                    startActivity(intent);
                } // 날짜 필터만 선택되었을 때
                else if(chbox_hopeDate.isChecked() == true && chbox_faceOpen.isChecked() == false && chbox_locate.isChecked() == false) {
                    Intent intent = new Intent(Filter.this, DateFilter.class);
                    startActivity(intent);
                }
                // 위치 필터만 선택되었을 때
                else if(chbox_locate.isChecked() == true && chbox_faceOpen.isChecked()==false && chbox_hopeDate.isChecked() == false) {
                    Intent intent = new Intent(Filter.this, GalleryList.class);
                    startActivity(intent);
                }

            }
        });

        //위치에 따른 사진 분류
        chbox_locate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 실행할 코드 여기에다가 적기

            }
        });
        
        //얼굴이 보이는 사진
        chbox_faceOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //실행할 코드 작성
            }
        });

        //눈 감은 사진
        chbox_eyeclosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 만약에 눈 감은 사진은 체크하고 얼굴이 보이는 사진을 체크하지 않았다면, 자동으로 체크되게 함
                if (chbox_faceOpen.isChecked() == false) {
                    chbox_faceOpen.setChecked(true);
                    // 이후에 실행할 코드 작성
                }
                //눈 감은 사진이 체크되어 있으면 얼굴이 보이는 사진 체크는 무조건 True가 되도록 함
                if(chbox_eyeclosed.isChecked() == true) {
                    chbox_faceOpen.setEnabled(false);
                }
                //눈 감은 사진이 체크가 해제되면 얼굴 보이는 사진 값을 바꿀 수 있도록 한다.
                if(chbox_eyeclosed.isChecked() == false) {
                    chbox_faceOpen.setEnabled(true);
                }
            }
        });

        // 날짜별로 분류
        chbox_hopeDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 이미지 선택 허용
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //radiobtn_all.setVisibility(View.VISIBLE);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Uri> selectedImages = new ArrayList<>();

            if(data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for(int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(imageUri);
                }
            } else {
                Uri imageUri = data.getData();
                selectedImages.add(imageUri);
            }

            // 선택한 이미지들에 대한 작업 수행
            // 이후 필요에 따라 선택한 이미지들에 대한 추가적인 처리를 수행할 수 있습니다.
        }
    }

}