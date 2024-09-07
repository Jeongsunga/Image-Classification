package com.example.picutre;
// 사용자가 인앱갤러리에서 폴더를 하나 선택하면
// gridView에 사진을 3장씩 보이게 하는 코드

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> imagePaths;
    private LayoutInflater inflater;
    private RequestManager glideRequestManager;

    public ImageAdapter(Context context, List<String> imagePaths, RequestManager glideRequestManager) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.glideRequestManager = glideRequestManager;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);

        // 이미지 파일 경로를 통해 이미지 로드 (Glide를 사용하여 이미지 로딩)
        String imagePath = imagePaths.get(position);
        Log.d("ImageAdapter", "Loading image from URL: " + imagePath);
        glideRequestManager.load(imagePath).into(imageView); // 이미지 로드

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageOne.class);
            intent.putStringArrayListExtra("imagePaths", new ArrayList<>(imagePaths));
            Log.d(TAG, "imagesUrl list[] : " + imagePaths);
            intent.putExtra("position", position); // 클릭된 이미지의 위치 전달
            intent.putExtra("selectImageUrl", imagePaths.get(position)); //이미지 리스트 중 사용자가 선택한 이미지의 링크만
            context.startActivity(intent);
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(context, "long click", Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        return convertView;
    }
}
