package com.example.picutre.ui.adapter;
// 사용자의 갤러리 요소들을 보여주는데 사용되는 어댑터 클래스


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.example.picutre.model.FolderItem;
import com.example.picutre.R;
import com.example.picutre.ui.activity.LoadingScreen;

import java.io.File;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<FolderItem> folderItems;
    private Context context;
    public FolderAdapter(List<FolderItem> folderItems ) {
        this.folderItems = folderItems;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.listview, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.FolderViewHolder holder, int position) {
        FolderItem folderItem = folderItems.get(position);
        holder.folderName.setText(folderItem.getFolderName());
        holder.count.setText(String.valueOf(folderItem.getCount()));
        Glide.with(context).load(folderItem.getFirstImagePath()).into(holder.folderImage);

        // 사용자가 분류할 폴더를 선택했을 때 실행되는 코드
        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("알림");
            builder.setMessage("데이터가 많으면 화면이 꺼지거나 로딩이 느릴 수 있으나 문제가 아닙니다.");
            builder.setPositiveButton("확인", (dialog, which) -> {
                dialog.dismiss();
                Intent intent = new Intent(context, LoadingScreen.class); //로딩 스크린으로 화면 이동
                String folderPath = new File(folderItem.getFirstImagePath()).getParent(); // 이미지 경로에서 폴더 경로 추출
                intent.putExtra("folderPath", folderPath);
                intent.putExtra("folderName", folderItem.getFolderName());
                context.startActivity(intent);
            });
            builder.setNegativeButton("취소", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return folderItems.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageView folderImage;
        TextView count;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.name);
            folderImage = itemView.findViewById(R.id.imageview);
            count = itemView.findViewById(R.id.count);
        }
    }
}
