package com.example.picutre.ui.adapter;
// 사용자가 인앱갤러리에서 폴더를 하나 선택하면 gridView에 사진을 보이게 하는 코드

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.example.picutre.R;
import com.example.picutre.ui.activity.ImageOne;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> imagePaths;
    private LayoutInflater inflater;
    private RequestManager glideRequestManager;
    private static final int DELETE_PHOTO_REQUEST_CODE = 1001;  // 동일한 값 사용
    private ArrayList<Integer> selectedItems = new ArrayList<>(); // 선택된 항목 저장

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
        if (position >= 0 && position < imagePaths.size()) {
            return imagePaths.get(position);
        }
        return null; // 또는 적절한 기본값 반환
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
            ((Activity) context).startActivityForResult(intent, DELETE_PHOTO_REQUEST_CODE);
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(Integer.valueOf(position));
                } else {
                    selectedItems.add(position);
                }
                notifyDataSetChanged(); // UI 갱신
                return true;
            }
        });

        // 선택된 상태일 때 UI 업데이트
        if (selectedItems.contains(position)) {
            imageView.setColorFilter(Color.argb(150, 0, 0, 0)); // 선택된 경우 어둡게 처리
        } else {
            imageView.clearColorFilter();
        }

        return convertView;
    }

    // 새로운 데이터로 업데이트하는 메서드
    public void updateData(List<String> newImageLinks) {
        Log.d(TAG, "기존의 이미지 링크 리스트: " + imagePaths);
        this.imagePaths.clear();
        if (newImageLinks != null) {
            this.imagePaths.addAll(newImageLinks);
            Log.d(TAG, "새로운 이미지 링크 리스트: " + imagePaths);
        }
        notifyDataSetChanged(); // 데이터 변경 알림
    }

    // 선택된 이미지들 반환
    public ArrayList<String> getSelectedImages() {
        ArrayList<String> selectedImages = new ArrayList<>();
        for (int index : selectedItems) {
            selectedImages.add(imagePaths.get(index));
        }
        return selectedImages;
    }

    // 선택 초기화
    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }
}
