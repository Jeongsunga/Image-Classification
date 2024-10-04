package com.example.picutre.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.picutre.R;
import com.example.picutre.model.OnlyFilterNumber;
import com.example.picutre.model.ResponseData;
import com.example.picutre.network.interfaces.FilterNumber;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.fragment.Fragment1;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Filter extends AppCompatActivity {

    RelativeLayout btn1, btn2, btn3, btn4;
    RelativeLayout nextButton;
    private FilterNumber filterNumber;  //api interface
    private static final int REQUEST_CODE = 1;
    private int selectedFilter = -1; // Default value indicating no selection
    String baseUrl = "YOUR_BASE_URL_HERE"; // Add your base URL here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);

        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
        filterNumber = retrofit.create(FilterNumber.class);

        btn1 = (RelativeLayout) findViewById(R.id.chbox_faceOpen);
        btn2 = (RelativeLayout) findViewById(R.id.chbox_eyeclosed);
        btn3 = (RelativeLayout) findViewById(R.id.chbox_hopeDate);
        btn4 = (RelativeLayout) findViewById(R.id.chbox_locate);
        nextButton = findViewById(R.id.btn_next);

        // 버튼 클릭 시 이벤트 설정
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment1 = new Fragment1();
                transaction.replace(R.id.frame, fragment1);
                transaction.addToBackStack(null);
                transaction.commit();

                selectedFilter = 1; // Face filter
                highlightSelection(btn1);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment2 = new Fragment1();
                transaction.replace(R.id.frame, fragment2);
                transaction.addToBackStack(null);
                transaction.commit();

                selectedFilter = 2; // Eyes filter
                highlightSelection(btn2);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment3 = new Fragment1();
                transaction.replace(R.id.frame, fragment3);
                transaction.addToBackStack(null);
                transaction.commit();

                selectedFilter = 3; // Date filter
                highlightSelection(btn3);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment4 = new Fragment1();
                transaction.replace(R.id.frame, fragment4);
                transaction.addToBackStack(null);
                transaction.commit();

                selectedFilter = 4; // Location filter
                highlightSelection(btn4);
            }
        });

        // Next button click event
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFilter == -1) {
                    // No filter selected, show a toast message
                    Toast.makeText(Filter.this, "Please select a filter", Toast.LENGTH_SHORT).show();
                } else {
                    // Send the selected filter to the server
                    sendDataToServer(selectedFilter);
                }
            }
        });
    }

    // Highlight the selected RelativeLayout to show it's been chosen
    private void highlightSelection(RelativeLayout selectedLayout) {
        // Reset background colors for all buttons (optional)
        btn1.setBackgroundColor(getResources().getColor(R.color.purple));
        btn2.setBackgroundColor(getResources().getColor(R.color.purple));
        btn3.setBackgroundColor(getResources().getColor(R.color.purple));
        btn4.setBackgroundColor(getResources().getColor(R.color.purple));

        // Highlight the selected button (you can change the color to fit your design)
        selectedLayout.setBackgroundColor(getResources().getColor(R.color.light_purple));
    }

    // Method to send the selected filter to the server
    private void sendDataToServer(int filter) {
        OnlyFilterNumber onlyFilterNumber = new OnlyFilterNumber(filter);

        Call<ResponseData> call = filterNumber.sendData(onlyFilterNumber);
        call.enqueue(new Callback<ResponseData>() { // 콜백 추가
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    ResponseData myResponse = response.body();
                    Log.d("Filter", "Success: " + myResponse.getMessage());
                    // Proceed to the next activity based on the selected filter
                    navigateToNextActivity(filter);
                } else {
                    Log.d("Filter", "Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e("Filter", "Error: " + t.getMessage());
            }
        });
    }

    // Navigate to the appropriate activity based on the selected filter
    private void navigateToNextActivity(int filter) {
        Intent intent;
        if (filter == 1 || filter == 2 || filter == 4) {
            // For face, eyes, or location filter, navigate to GalleryList
            intent = new Intent(Filter.this, GalleryList.class);
        } else {
            // For date filter, navigate to DateFilter
            intent = new Intent(Filter.this, DateFilter.class);
        }
        startActivity(intent);
    }
}
