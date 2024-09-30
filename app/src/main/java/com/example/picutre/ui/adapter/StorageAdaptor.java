package com.example.picutre.ui.adapter;
// 서버에 저장되어 있는 분류 완료 폴더들을 화면에 보여주는 코드


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.FolderItem;
import com.example.picutre.model.ResponseData;
import com.example.picutre.model.StorageItem;
import com.example.picutre.network.interfaces.DeleteFolderApi;
import com.example.picutre.network.interfaces.ImageApi;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.activity.FirebaseStorage_images;
import com.example.picutre.ui.activity.InAppGallery;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StorageAdaptor extends RecyclerView.Adapter<StorageAdaptor.StorageItemViewHolder> {

    private List<StorageItem> storageItemList;
    private Context context;
    private ImageApi imageApi;
    private DeleteFolderApi deleteFolderApi;
    String baseUrl = BaseURL.BASE_URL;
    Retrofit retrofit = RetrofitClient.getClient(baseUrl);

    public StorageAdaptor(List<StorageItem> storageItemList) {
        this.storageItemList = storageItemList;
    }
    public StorageAdaptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public StorageItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview, parent, false);
        return new StorageItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StorageItemViewHolder holder, int position) {

        StorageItem storageItem = storageItemList.get(position);
        holder.folderNameTextView.setText(storageItem.getFolderName2());
        holder.countTextView.setText(String.valueOf(storageItem.getCount2()));
        Picasso.get()
                .load(storageItem.getFirstImagePath2())
                //.resize(75, 75) // 원하는 크기로 조정
                //.centerInside() // 크기 조정 후, 이미지가 중앙에 위치하도록 설정
                .into(holder.firstImageView);

        // 사용자가 폴더를 선택했을 때, 폴더의 이름과 사진 장수를 FirebaseStorage_images로 값을 전달
        holder.itemView.setOnClickListener(v -> {

            Context context = holder.itemView.getContext(); // Context 얻기
            Intent intent = new Intent(context, FirebaseStorage_images.class);

            String folderPath = storageItem.getFolderName2();
            int lastSlashIndex = folderPath.lastIndexOf('/');
            String lastSegment = folderPath.substring(lastSlashIndex + 1);

            intent.putExtra("folderName", lastSegment);
            intent.putExtra("imageCount", storageItem.getCount2());

            context.startActivity(intent);
        });

        // 사용자가 폴더를 길게 누를 때 삭제 or 다운 받을 수 있게 함
        holder.itemView.setOnLongClickListener(v -> {
            //Log.d(TAG, "선택한 폴더 이름: " + storageItem.getFolderName2());
            Context context = v.getContext();  // Context 가져오기

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(storageItem.getFolderName2() + "을(를) 선택했습니다.");
            builder.setPositiveButton("저장", (dialog, which) -> {
                dialog.dismiss();

                imageApi = retrofit.create(ImageApi.class);
                Call<List<String>> call = imageApi.getImages(storageItem.getFolderName2());
                call.enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if(response != null && response.isSuccessful()) {
                            List<String> imageUrls = response.body();
                            Log.d(TAG, "이미지 링크 리스트: " + imageUrls);
                            InAppGallery inAppGallery = new InAppGallery();
                            inAppGallery.downloadImage(imageUrls, context);
                            Toast.makeText(context, "저장 완료", Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d(TAG, "응답에 실패했거나 바디가 비었습니다.");
                            Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Log.d(TAG, "응답 에러: " + t.getMessage());
                        Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show();
                    }
                });

            });
            builder.setNegativeButton("삭제", (dialog, which) -> {
                dialog.dismiss();
                deleteFolderApi = retrofit.create(DeleteFolderApi.class);
                Call<ResponseData> call = deleteFolderApi.deleteFolder(storageItem.getFolderName2());
                call.enqueue(new Callback<ResponseData>() {
                    @Override
                    public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                        if(response.isSuccessful() && response.body() != null) {
                            // 파이어베이스 삭제하는 코드 작성
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference databaseRef = database.getReference(storageItem.getFolderName2());
                            databaseRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show();
                                    else Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                            
                            // 리사이클러뷰 새로고침하는 코드 작성
                            InAppGallery inAppGallery = new InAppGallery();
                            inAppGallery.fetchDataFromServer();
                        }else {
                            Log.d(TAG, "응답이 없거나 바디가 비었습니다.");
                            Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData> call, Throwable t) {
                        Log.d(TAG, "응답 실패: " + t.getMessage());
                        Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return storageItemList.size();
    }

    public class StorageItemViewHolder extends RecyclerView.ViewHolder {
        TextView folderNameTextView;
        TextView countTextView;
        ImageView firstImageView;

        public StorageItemViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.name);
            countTextView = itemView.findViewById(R.id.count);
            firstImageView = itemView.findViewById(R.id.imageview);
        }
    }

}
