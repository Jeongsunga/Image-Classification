package com.example.picutre.ui.adapter;

import static androidx.constraintlayout.widget.Constraints.TAG;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.picutre.R;
import com.example.picutre.constants.BaseURL;
import com.example.picutre.model.ResponseData;
import com.example.picutre.model.StorageItem;
import com.example.picutre.network.interfaces.FolderNameApi;
import com.example.picutre.network.retrofit.RetrofitClient;
import com.example.picutre.ui.activity.LoadingScreen;
import com.example.picutre.ui.activity.NewLoadingScreen;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ServerFolderAdapter extends RecyclerView.Adapter<ServerFolderAdapter.StorageItemViewHolder> {

    private List<StorageItem> storageItemList;
    private Context context;
    private FolderNameApi folderNameApi;
    String baseUrl = BaseURL.BASE_URL;
    Retrofit retrofit = RetrofitClient.getClient(baseUrl);

    public ServerFolderAdapter(List<StorageItem> storageItemList) {
        this.storageItemList = storageItemList;
    }

    public ServerFolderAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ServerFolderAdapter.StorageItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview, parent, false);
        return new StorageItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerFolderAdapter.StorageItemViewHolder holder, int position) {
        StorageItem storageItem = storageItemList.get(position);
        holder.folderNameTextView.setText(storageItem.getFolderName2());
        holder.countTextView.setText(String.valueOf(storageItem.getCount2()));
        Picasso.get()
                .load(storageItem.getFirstImagePath2())
                //.resize(75, 75) // 원하는 크기로 조정
                //.centerInside() // 크기 조정 후, 이미지가 중앙에 위치하도록 설정
                .into(holder.firstImageView);

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, NewLoadingScreen.class);
            intent.putExtra("folderName", storageItem.getFolderName2());
            context.startActivity(intent);
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
