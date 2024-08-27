package com.example.picutre;
// 파이어베이스 스토리지에 저장되어 있는 폴더들에 관한 이벤트 수행

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class StorageAdaptor extends RecyclerView.Adapter<StorageAdaptor.StorageItemViewHolder> {

    private List<StorageItem> storageItemList;
    private Context context;

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

                if (storageItem.getFirstImagePath2() != null) {
                    
                    Glide.with(holder.itemView.getContext())
                            .load(storageItem.getFirstImagePath2())
                            .into(holder.firstImageView);

                }else {
                    holder.firstImageView.setImageResource(R.drawable.clover); // placeholder 이미지 설정
                    //holder.folderNameTextVciew.setText("파이어베이스 스토리지에 저장된 데이터가 없습니다.");
                }

        // 사용자가 폴더를 선택했을 때, 폴더의 이름을 FirebaseStorage_images로 값을 전달
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

    }

    @Override
    public int getItemCount() {
        return storageItemList.size();
    }

    public static class StorageItemViewHolder extends RecyclerView.ViewHolder {
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
