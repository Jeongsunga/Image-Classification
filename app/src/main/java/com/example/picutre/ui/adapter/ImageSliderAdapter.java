package com.example.picutre.ui.adapter;
// 사진 한장만 보이는 화면 어댑터

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.LinkAndHeart;
import com.example.picutre.model.Metadatas;
import com.example.picutre.R;
import com.example.picutre.network.interfaces.ImageUrlApi;
import com.example.picutre.network.interfaces.ServerCallback;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.activity.ImageOne;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>{

    private List<String> imageUrls;
    private Context context;
    private ImageUrlApi imageUrlApi;
    String infoes;
    private OnItemClickListener listener;
    String baseurl = BaseURL.BASE_URL;
    Retrofit retrofit = RetrofitClient.getClient(baseurl);
    private LinkAndHeart linkAndHeart;

    public ImageSliderAdapter(List<String> imageUrls, Context context, LinkAndHeart linkAndHeart, OnItemClickListener listener) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.linkAndHeart = linkAndHeart;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_silder, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.loading2)
                .error(R.drawable.error)
                .into(holder.imageView);

        Log.d(TAG, "linkAndHeart hash2: " + linkAndHeart.getImageUrl() + "boolean2: " + linkAndHeart.isHeart());

        // 하트 상태에 따라 버튼 이미지 설정
        if (linkAndHeart.isHeart()) {
            holder.btn_heart.setImageResource(R.drawable.fullheart);  // 좋아요가 눌린 상태
        } else {
            holder.btn_heart.setImageResource(R.drawable.heart);  // 좋아요가 눌리지 않은 상태
        }
        ImageOne imageOne = new ImageOne();

        SharedPreferences sharedPreferences = context.getSharedPreferences("folderName", context.MODE_PRIVATE);
        String value = sharedPreferences.getString("foldername", "default");
        Log.d(TAG, "sharedPreferences: " + value);

        holder.btn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String urlToHash = imageOne.getHash(imageUrl);
                Map<String, Object> updates = new HashMap<>();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference(value);
                if(linkAndHeart.isHeart()) {
                    updates.put(urlToHash, false);  // 좋아요 값 변경
                    databaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // 업데이트 성공
                                linkAndHeart.setHeart(false);  // 좋아요 상태 변경
                                Log.d(TAG, "파이어베이스 업데이트 성공");
                            } else {
                                // 업데이트 실패
                                Log.d(TAG, "파이어베이스 업데이트 실패");
                            }
                        }
                    });
                    holder.btn_heart.setImageResource(R.drawable.heart);  // 좋아요 O -> X

                }else {
                    updates.put(urlToHash, true);  // 좋아요 값 변경
                    databaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // 업데이트 성공
                                linkAndHeart.setHeart(true);  // 좋아요 상태 변경
                                Log.d(TAG, "파이어베이스 업데이트 성공");
                            } else {
                                // 업데이트 실패
                                Log.d(TAG, "파이어베이스 업데이트 실패");
                            }
                        }
                    });
                    holder.btn_heart.setImageResource(R.drawable.fullheart);  // 좋아요 X -> O

                }
            }
        });

        holder.btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownloadClick(position);
                //showDownloadDialog(imageUrl);
            }
        });

        holder.btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 비동기적으로 서버에서부터 데이터를 받음
                sendImageUrlToServer(imageUrl, new ServerCallback() {
                    @Override
                    public void onResponseReceived(String info) {
                        new AlertDialog.Builder(context)
                                .setTitle("사진 정보")
                                .setMessage(info).show();
                    }
                });
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showDeleteDialog(imageUrl);
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public interface OnItemClickListener {
        void onHeartClick(int position);
        void onInfoClick(int position);
        void onDownloadClick(int position);
        void onDeleteClick(int position);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btn_heart, btn_info, btn_download, btn_delete;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btn_heart = itemView.findViewById(R.id.btn_heart);
            btn_info = itemView.findViewById(R.id.btn_info);
            btn_download = itemView.findViewById(R.id.btn_download);
            btn_delete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public void sendImageUrlToServer(String imageUrl, ServerCallback callback) {
        imageUrlApi = retrofit.create(ImageUrlApi.class);
        Call<Metadatas> call = imageUrlApi.sendAPI(imageUrl);
        call.enqueue(new Callback<Metadatas>() {
            @Override
            public void onResponse(Call<Metadatas> call, Response<Metadatas> response) {
                if(response.isSuccessful() && response.body() != null) {
                    Metadatas metadatas = response.body();

                    String fileName = metadatas.getFileName();
                    String fileSize = metadatas.getFileSize();
                    String captureDate = metadatas.getCaptureDate();
                    String address = metadatas.getAddress();

                    infoes = "파일이름 : " + fileName + "\n파일 크기 : " + fileSize + "\n촬영 시간 : " + captureDate +
                            "\n촬영 위치 : " + address;
                    Log.d(TAG, "메타데이터: " + infoes);
                    callback.onResponseReceived(infoes);
                } else {
                    Log.d(TAG, "응답 바디가 잘못됨.");
                }
            }

            @Override
            public void onFailure(Call<Metadatas> call, Throwable t) {
                Log.d(TAG, "응답이 아예 오지 않음.");
            }
        });
    }
}
