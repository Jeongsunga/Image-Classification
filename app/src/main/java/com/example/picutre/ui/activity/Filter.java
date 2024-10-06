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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Highlight the selected RelativeLayout to show it's been chosen
    private void highlightSelection(TextView selectedTextView) {
        // Reset all TextViews to the default background and text color
        resetTextViews();

        // Set the selected TextView's text color to black, keeping its current background
        selectedTextView.setTextColor(Color.BLACK);
    }

    // Reset all TextViews to their default state
    private void resetTextViews() {
        // Reset the text color and background for each TextView
        tv1.setTextColor(Color.WHITE);
        tv1.setBackgroundResource(R.drawable.arc_shape_purple);

        tv2.setTextColor(Color.WHITE);
        tv2.setBackgroundResource(R.drawable.arc_shape_purple);

        tv3.setTextColor(Color.WHITE);
        tv3.setBackgroundResource(R.drawable.arc_shape_purple);

        tv4.setTextColor(Color.WHITE);
        tv4.setBackgroundResource(R.drawable.arc_shape_purple);
    }

//    // Highlight the selected RelativeLayout to show it's been chosen
//    private void highlightSelection(TextView selectedTextView) {
//        if(selectedTextView == tv1) {
//            selectedTextView.setBackgroundColor(Color.WHITE);
//            selectedTextView.setTextColor(Color.BLACK);
//
//            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv2.setTextColor(Color.WHITE);
//            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv3.setTextColor(Color.WHITE);
//            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv4.setTextColor(Color.WHITE);
//        }
//        else if(selectedTextView == tv2) {
//            selectedTextView.setBackgroundColor(Color.WHITE);
//            selectedTextView.setTextColor(Color.BLACK);
//
//            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv1.setTextColor(Color.WHITE);
//            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv3.setTextColor(Color.WHITE);
//            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv4.setTextColor(Color.WHITE);
//        }else if(selectedTextView == tv3) {
//            selectedTextView.setBackgroundColor(Color.WHITE);
//            selectedTextView.setTextColor(Color.BLACK);
//
//            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv2.setTextColor(Color.WHITE);
//            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv1.setTextColor(Color.WHITE);
//            tv4.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv4.setTextColor(Color.WHITE);
//        }else {
//            selectedTextView.setBackgroundColor(Color.WHITE);
//            selectedTextView.setTextColor(Color.BLACK);
//
//            tv2.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv2.setTextColor(Color.WHITE);
//            tv3.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv3.setTextColor(Color.WHITE);
//            tv1.setBackgroundColor(Color.argb(255, 81, 0, 141)); //보라색
//            tv1.setTextColor(Color.WHITE);
//        }
//    }

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