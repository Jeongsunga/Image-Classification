package com.example.picutre.ui.activity;
// 사용자가 분류하고자 하는 방식을 정하는 화면(2번)

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.OnlyFilterNumber;
import com.example.picutre.R;
import com.example.picutre.model.ResponseData;
import com.example.picutre.network.interfaces.FilterNumber;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.fragment.Fragment1;
import com.example.picutre.ui.fragment.Fragment2;
import com.example.picutre.ui.fragment.Fragment3;
import com.example.picutre.ui.fragment.Fragment4;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.Response;

public class Filter extends AppCompatActivity {

    RelativeLayout btn1, btn2, btn3, btn4, nextButton;
    private FilterNumber filterNumber;
    String baseUrl = BaseURL.BASE_URL;
    private int selectedFilter = -1; // Default value indicating no selection
    TextView tv1, tv2, tv3, tv4;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);

        nextButton = findViewById(R.id.btn_next);
        btn4 = findViewById(R.id.chbox_locate);
        btn2 = findViewById(R.id.chbox_eyeclosed);
        btn1 = findViewById(R.id.chbox_faceOpen);
        btn3 = findViewById(R.id.chbox_hopeDate);

        tv1 = findViewById(R.id.tv_face);
        tv2 = findViewById(R.id.tv_eye);
        tv3 = findViewById(R.id.tv_date);
        tv4 = findViewById(R.id.tv_location);

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
        filterNumber = retrofit.create(FilterNumber.class);

        btn1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment1 = new Fragment1();
                transaction.replace(R.id.frame, fragment1);
                transaction.addToBackStack(null);
                transaction.commit();
                selectedFilter = 1; // Face filter
                highlightSelection(tv1);
                //tv1.setBackgroundColor(Color.WHITE);
                //tv1.setTextColor(Color.BLACK);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment2 fragment2 = new Fragment2();
                transaction.replace(R.id.frame, fragment2);
                transaction.addToBackStack(null);
                transaction.commit();
                selectedFilter = 2; // Eyes filter
                highlightSelection(tv2);

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment3 fragment3 = new Fragment3();
                transaction.replace(R.id.frame, fragment3);
                transaction.addToBackStack(null);
                transaction.commit();
                selectedFilter = 3; // Date filter
                highlightSelection(tv3);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment4 fragment4 = new Fragment4();
                transaction.replace(R.id.frame, fragment4);
                transaction.addToBackStack(null);
                transaction.commit();
                selectedFilter = 4; // Location filter
                highlightSelection(tv4);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFilter == -1) {
                    Toast.makeText(Filter.this, "Please select a filter", Toast.LENGTH_SHORT).show();
                } else {
                    sendDataToServer(selectedFilter);
                    if(selectedFilter == 3) {
                        Intent intent = new Intent(Filter.this, DateFilter.class);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(Filter.this, GalleryList.class);
                        startActivity(intent);
                    }
                }
            }
        });

//        btn_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //분류 필터를 한 가지도 선택하지 않았을 때 토스트 문구 알림
//                if(!chbox_faceOpen.isChecked() && !chbox_eyeclosed.isChecked() &&
//                        !chbox_hopeDate.isChecked() && !chbox_locate.isChecked()) {
//                    Toast toast = Toast.makeText(getApplicationContext(), "필터를 한 가지 선택해 주세요.",Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//                /*
//                선택할 수 있는 필터
//                1. 얼굴 보이기 넘기는 값 :1
//                2. 눈 뜨기 : 2
//                3. 날짜 : 3
//                4. 위치 : 4
//
//                사용자가 선택한 분류 방식에 따라 서버에 다른 값을 넘겨주어 분류 파이썬 코드를 돌아가게 한다. */
//                // 얼굴 보이기와 다른 필터를 하나 선택했을 때
//                if(chbox_faceOpen.isChecked() && (chbox_locate.isChecked() || chbox_hopeDate.isChecked() || chbox_eyeclosed.isChecked())) {
//                    Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT).show();
//
//                // 날짜와 다른 필터 하나를 선택했을 때
//                }else if(chbox_hopeDate.isChecked() && (chbox_faceOpen.isChecked() || chbox_locate.isChecked() || chbox_eyeclosed.isChecked())) {
//                    Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT).show();
//                // 지역과 다른 필터 하나를 선택했을 때
//                }else if(chbox_locate.isChecked() && (chbox_faceOpen.isChecked() || chbox_hopeDate.isChecked() || chbox_eyeclosed.isChecked())) {
//                    Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT).show();
//                // 눈과 다른 필터 하나를 선택했을 때
//                } else if (chbox_eyeclosed.isChecked() && (chbox_faceOpen.isChecked() || chbox_hopeDate.isChecked() || chbox_locate.isChecked())) {
//                    Toast.makeText(getApplicationContext(), "필터를 한 가지만 선택해 주세요.",Toast.LENGTH_SHORT).show();
//                }
//
//                //얼굴 보이기 필터만 선택되었을 때
//                if(chbox_faceOpen.isChecked() && !chbox_hopeDate.isChecked() && !chbox_locate.isChecked() && !chbox_eyeclosed.isChecked()) {
//
//                    sendDataToServer(1);
//
//                } // 날짜 필터만 선택되었을 때
//                else if(chbox_hopeDate.isChecked() && !chbox_faceOpen.isChecked() && !chbox_locate.isChecked() && !chbox_eyeclosed.isChecked()) {
//                    Intent intent = new Intent(Filter.this, DateFilter.class);
//                    sendDataToServer(3);
//                    startActivity(intent);
//                }
//                // 위치 필터만 선택되었을 때
//                else if(chbox_locate.isChecked() && !chbox_faceOpen.isChecked() && !chbox_hopeDate.isChecked() && !chbox_eyeclosed.isChecked()) {
//                    sendDataToServer(4);
//                    Intent intent = new Intent(Filter.this, GalleryList.class);
//                    startActivity(intent);
//                }
//                //  눈 보이기 필터가 선택되었을 때
//                else if(chbox_eyeclosed.isChecked() && !chbox_hopeDate.isChecked() && !chbox_locate.isChecked() && !chbox_faceOpen.isChecked()) {
//                    sendDataToServer(2);
//                    Intent intent = new Intent(Filter.this, GalleryList.class);
//                    startActivity(intent);
//                }
//
//            }
//        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Highlight the selected RelativeLayout to show it's been chosen
    private void highlightSelection(TextView selectedTextView) {
        if(selectedTextView == tv1) {
            selectedTextView.setBackgroundColor(Color.WHITE);
            selectedTextView.setTextColor(Color.BLACK);

            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv2.setTextColor(Color.WHITE);
            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv3.setTextColor(Color.WHITE);
            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv4.setTextColor(Color.WHITE);
        }
        else if(selectedTextView == tv2) {
            selectedTextView.setBackgroundColor(Color.WHITE);
            selectedTextView.setTextColor(Color.BLACK);

            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv1.setTextColor(Color.WHITE);
            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv3.setTextColor(Color.WHITE);
            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv4.setTextColor(Color.WHITE);
        }else if(selectedTextView == tv3) {
            selectedTextView.setBackgroundColor(Color.WHITE);
            selectedTextView.setTextColor(Color.BLACK);

            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv2.setTextColor(Color.WHITE);
            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv1.setTextColor(Color.WHITE);
            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv4.setTextColor(Color.WHITE);
        }else {
            selectedTextView.setBackgroundColor(Color.WHITE);
            selectedTextView.setTextColor(Color.BLACK);

            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv2.setTextColor(Color.WHITE);
            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv3.setTextColor(Color.WHITE);
            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
            tv1.setTextColor(Color.WHITE);
        }
    }

    private void sendDataToServer(int num) {
        // 요청 데이터 생성
        OnlyFilterNumber onlyFilterNumber = new OnlyFilterNumber(num);

        // 서버로 요청 보내기
        Call<ResponseData> call = filterNumber.sendData(onlyFilterNumber);
        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    ResponseData myResponse = response.body();
                    Log.d("Filter", "Success123456: " + myResponse.getMessage());
                } else {
                    Log.d("Filter", "Request failed123456: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e("Filter", "Error123456: " + t.getMessage());
            }
        });
    }
}