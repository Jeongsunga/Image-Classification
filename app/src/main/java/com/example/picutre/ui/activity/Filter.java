package com.example.picutre.ui.activity;
// 사용자가 분류하고자 하는 방식을 정하는 화면(2번)

//import static com.example.picutre.network.retrofit.RetrofitClient.retrofit;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.picutre.R;
import com.example.picutre.ui.fragment.Fragment1;

public class Filter extends AppCompatActivity {

    RelativeLayout btn1, btn2, btn3, btn4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);

        btn1 = (RelativeLayout) findViewById(R.id.chbox_faceOpen);
        btn1 = (RelativeLayout) findViewById(R.id.chbox_eyeclosed);
        btn1 = (RelativeLayout) findViewById(R.id.chbox_hopeDate);
        btn1 = (RelativeLayout) findViewById(R.id.chbox_locate);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment1 fragment1 = new Fragment1();
                transaction.replace(R.id.frame, fragment1);
                transaction.addToBackStack(null);
                transaction.commit();
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
            }
        });
    }
//    Button btn_next;
//    CheckBox chbox_locate;
//    CheckBox chbox_eyeclosed;
//    CheckBox chbox_faceOpen;
//    CheckBox chbox_hopeDate;
//    private FilterNumber filterNumber; //api interface 임
//    private static final int REQUEST_CODE = 1;
//    String baseUrl = BaseURL.BASE_URL;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_filter);
//
//        btn_next = findViewById(R.id.btn_next);
//        chbox_locate = findViewById(R.id.chbox_locate);
//        chbox_eyeclosed = findViewById(R.id.chbox_eyeclosed);
//        chbox_faceOpen = findViewById(R.id.chbox_faceOpen);
//        chbox_hopeDate = findViewById(R.id.chbox_hopeDate);
//
//        Retrofit retrofit = RetrofitClient.getClient(baseUrl);
//        filterNumber = retrofit.create(FilterNumber.class);
//
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
//
//                사용자가 선택한 분류 방식에 따라 서버에 다른 값을 넘겨주어 분류 파이썬 코드를 돌아가게 한다.
//                 */
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
//                    Intent intent = new Intent(Filter.this, GalleryList.class);
//                    sendDataToServer(1);
//                    startActivity(intent);
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
//
////        //위치에 따른 사진 분류
////        chbox_locate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                // 실행할 코드 여기에다가 적기
////
////            }
////        });
////
////        //얼굴이 보이는 사진
////        chbox_faceOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                //실행할 코드 작성
////            }
////        });
////
////        //눈 감은 사진
////        chbox_eyeclosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////
////            }
////        });
////
////        // 날짜별로 분류
////        chbox_hopeDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
////            @Override
////            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////
////            }
////        });
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//
//    /*@Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            ArrayList<Uri> selectedImages = new ArrayList<>();
//
//            if(data.getClipData() != null) {
//                int count = data.getClipData().getItemCount();
//                for(int i = 0; i < count; i++) {
//                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
//                    selectedImages.add(imageUri);
//                }
//            } else {
//                Uri imageUri = data.getData();
//                selectedImages.add(imageUri);
//            }
//
//            // 선택한 이미지들에 대한 작업 수행
//            // 이후 필요에 따라 선택한 이미지들에 대한 추가적인 처리를 수행할 수 있습니다.
//        }
//    }*/
//
//    private void sendDataToServer(int num) {
//        // 요청 데이터 생성
//        OnlyFilterNumber onlyFilterNumber = new OnlyFilterNumber(num);
//
//        // 서버로 요청 보내기
//        Call<ResponseData> call = filterNumber.sendData(onlyFilterNumber);
//        call.enqueue(new Callback<ResponseData>() {
//            @Override
//            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
//                if (response.isSuccessful()) {
//                    ResponseData myResponse = response.body();
//                    Log.d("Filter", "Success123456: " + myResponse.getMessage());
//                } else {
//                    Log.d("Filter", "Request failed123456: " + response.code());
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseData> call, Throwable t) {
//                Log.e("Filter", "Error123456: " + t.getMessage());
//            }
//        });
//    }
}