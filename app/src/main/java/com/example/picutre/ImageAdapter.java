package com.example.picutre;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> imagePaths;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, ArrayList<String> imagePaths, LayoutInflater inflater) {
        this.context = context;
        this.imagePaths = imagePaths;
        inflater = LayoutInflater.from(context);
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
        Glide.with(context)
                .load(imagePath)
                .into(imageView);

        return convertView;
    }
}
