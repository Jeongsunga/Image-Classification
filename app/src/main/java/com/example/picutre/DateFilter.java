package com.example.picutre;
// 사용자가 날짜별로 분류를 선택했을 때
// 하루, 기간으로 할건지, 폴더 이름은 어떻게 할건지 설정하는 화면(2.5번 화면)

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.util.Pair;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;

public class DateFilter extends AppCompatActivity {

    private Button btn_next;
    private Button btn_oneday, btn_twoday;
    private TextView tv_date;
    Calendar calendar;
    private EditText editTitle;

    LinearLayout linearLayout;
    
    // dataString : 하루
    // dataString1 : 기간 시작일
    // dataString2 : 기간 마감일
    String dateString1, dateString2, dateString, innoDate;

    PeriodAndDate periodAndDate;
    int periodNumber;
    String foldername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_datefilter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://172.21.195.40:5000/")  // 로컬 호스트 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        periodAndDate = retrofit.create(PeriodAndDate.class);

        linearLayout = findViewById(R.id.group_title2);

        btn_next = findViewById(R.id.btn_next);
        tv_date = findViewById(R.id.tv_date);
        btn_oneday = findViewById(R.id.btn_oneday);
        btn_twoday = findViewById(R.id.btn_twoday);
        editTitle = findViewById(R.id.period_title);
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        //오늘 날짜
        Long today = MaterialDatePicker.todayInUtcMilliseconds();

        //날짜 하루 선택 버튼
        btn_oneday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Date Picker")
                        .setSelection(today).build(); // 오늘 날짜 셋팅

                materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");

                // 사용자가 달력에서 날짜를 선택하고 확인 버튼을 눌렀을 때 일어나는 동작
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {

                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.setTimeInMillis(selection);
                        Calendar currentCalendar = Calendar.getInstance();

                        // 미래 날짜는 선택 불가 하도록 함
                        if(selectedCalendar.after(currentCalendar)) {
                            Toast toast = Toast.makeText(getApplicationContext(), "미래의 날짜는 선택 불가합니다.\n다시 선택해 주세요.",Toast.LENGTH_SHORT);
                            toast.show();
                            MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.datePicker()
                                    .setTitleText("Date Picker")
                                    .setSelection(today).build(); // 오늘 날짜 셋팅
                            materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                            if (selectedCalendar.before(currentCalendar)){
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                                Date date = new Date();
                                date.setTime(selection);

                                dateString = simpleDateFormat.format(date);
                                tv_date.setText(dateString);
                            }

                            // 당일 까지 선택 가능
                        }else if (selectedCalendar.before(currentCalendar)){
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                            Date date = new Date();
                            date.setTime(selection);

                            dateString = simpleDateFormat.format(date);
                            tv_date.setText(dateString);
                        }
                    }
                });
                periodNumber = 1;

            }
        });

        //기간 선택 버튼
        btn_twoday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Date Picker").build();

                materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");

                //확인버튼
                materialDatePicker.addOnPositiveButtonClickListener
                        (new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                            @Override
                            public void onPositiveButtonClick(Pair<Long, Long> selection) {

                                Calendar selectedCalendar1 = Calendar.getInstance();
                                selectedCalendar1.setTimeInMillis(selection.first);
                                Calendar selectedCalendar2 = Calendar.getInstance();
                                selectedCalendar2.setTimeInMillis(selection.second);
                                Calendar currentCalendar = Calendar.getInstance();

                                //양 일이 같은 날짜일 때
                                if(selectedCalendar1.equals(selectedCalendar2) ) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "같은 날짜는 선택 불가합니다.\n다시 선택해 주세요.",Toast.LENGTH_SHORT);
                                    toast.show();

                                    MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                                            .setTitleText("Date Picker").build();

                                    materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                                    if(!selectedCalendar1.equals(selectedCalendar2)) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

                                        Date date1 = new Date();
                                        Date date2 = new Date();

                                        date1.setTime(selection.first);
                                        date2.setTime(selection.second);

                                        dateString1 = simpleDateFormat.format(date1);
                                        dateString2 = simpleDateFormat.format(date2);
                                      
                                        tv_date.setText("시작일 : " + dateString1 + "\n" + "마감일 : " + dateString2);
                                    }


                                //시작일이나 마감일이 미래일 때
                                }else if(selectedCalendar1.after(currentCalendar) || selectedCalendar2.after(currentCalendar)) {
                                    Toast toast = Toast.makeText(getApplicationContext(), "미래 날짜는 선택 불가합니다.\n다시 선택해 주세요.",Toast.LENGTH_SHORT);
                                    toast.show();

                                    MaterialDatePicker materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                                            .setTitleText("Date Picker").build();

                                    materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                                    if(!(selectedCalendar1.after(currentCalendar) || selectedCalendar2.after(currentCalendar))) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

                                        Date date1 = new Date();
                                        Date date2 = new Date();

                                        date1.setTime(selection.first);
                                        date2.setTime(selection.second);

                                        dateString1 = simpleDateFormat.format(date1);
                                        dateString2 = simpleDateFormat.format(date2);
                                        tv_date.setText("시작일 : " + dateString1 + "\n" + "마감일 : " + dateString2);
                                    }
                                }

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

                                Date date1 = new Date();
                                Date date2 = new Date();

                                date1.setTime(selection.first);
                                date2.setTime(selection.second);

                                dateString1 = simpleDateFormat.format(date1);
                                dateString2 = simpleDateFormat.format(date2);
                                tv_date.setText("시작일 : " + dateString1 + "\n" + "마감일 : " + dateString2);
                            }
                        });
                periodNumber = 2;
            }
        });

        // 사용자가 입력한 날짜를 textview에 표시하고, 값이 변경된 후에 제목을 적을 수 있는 editText를 보이게 함
        tv_date.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 바뀌기 전
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 바뀌는 중
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 바뀐 후
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = tv_date.getText().toString();
                if(text.equals("날짜")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "날짜를 선택해주세요.",Toast.LENGTH_SHORT);
                    toast.show();
                }else {
//                    Log.d(TAG, "title length : " + editTitle.getText().length());
//                    if(editTitle.getText().length() == 0) { // 사용자가 분류 결과 생성될 폴더의 이름을 작성하지 않았다면
//
//                        // 사용자가 선택한 날짜로 폴더 이름을 생성하게 된다.
//                        // 서버로 사용자가 선택한 날짜값을 전송
//                        // 하루일 경우 -> dateString
//                        // 기간일 경우 -> dateString1 + ' - ' + dateString2
//                        if(periodNumber == 1) {
//                            // 하루
//                            innoDate = dateString; //sendDate : 순수 날짜
//                            foldername = dateString; //
//
//                        }else { // 기간
//                            foldername = dateString1 + '-' + dateString2;
//                            innoDate = foldername;
//                        }
//
//                    }else { // 사용자가 분류 결과 생성될 폴더의 이름을 작성했을 경우
//                        // 사용자가 작성한 문자열로 폴더를 생성하도록 값을 넘긴다.
//                        // 파라미터 -> editTitle.getText(), 새로운 변수에 값을 담아서 보내도 됨(자료형 String)
//                        foldername = (editTitle.getText().toString());
//                    }
                    if(periodNumber == 1) { // 하루일 때
                        innoDate = dateString;
                        if(editTitle.getText().length() == 0) { // 사용자 지정 폴더 이름 없음
                            foldername = dateString;
                        }else { // 사용자 지정 폴더 이름 있음
                            foldername = editTitle.getText().toString();
                        }
                    }else { //  기간일 때
                        innoDate = dateString1 + '-' + dateString2;
                        if(editTitle.getText().length() == 0) { // 사용자 지정 폴더 이름 없음
                            foldername = innoDate;
                        }else { // 사용자 지정 폴더 이름 있음
                            foldername = editTitle.getText().toString();
                        }
                    }

                    //Log.d(TAG, "folderName : " + foldername + " periodNumber : " + periodNumber);
                    sendDataToServer(periodNumber, foldername, innoDate);
                    Intent intent = new Intent(DateFilter.this, GalleryList.class);
                    startActivity(intent);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v,insets)->{
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendDataToServer(int periodNumber, String folderName, String innoDate) {
        // 요청 데이터 생성
        Log.d(TAG, "check the send data : " + periodNumber +  foldername +  innoDate);
        DateFolderName dataFolderName = new DateFolderName(periodNumber, folderName, innoDate);

        // 서버로 요청 보내기

        Call<ResponseData> call = periodAndDate.sendData(dataFolderName);

        call.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if (response.isSuccessful()) {
                    ResponseData myResponse = response.body();
                    Log.d("DateFilter", "Success123456789: " + myResponse.getMessage());
                } else {
                    Log.d("DateFilter", "Request failed123456789: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e("DateFilter", "Error123456789: " + t.getMessage());
            }
        });
    }
}