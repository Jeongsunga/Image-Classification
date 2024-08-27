package com.example.picutre;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirebaseImageLoader {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private List<String> imageUrls;

    public FirebaseImageLoader(String folderName) {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child(folderName + "/");
        imageUrls = new ArrayList<>();
    }

    public void fetchImageUrls(final ImageUrlCallback callback) {
        //Log.d(TAG, "어디서부터 잘못된건지");
        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    Log.d("FirebaseImageLoader", "Image URL: " + uri.toString()); // 로그 추가
                    if (imageUrls.size() == listResult.getItems().size()) {
                        Log.d(TAG, "here?");
                        callback.onSuccess(imageUrls);
                    }
                }).addOnFailureListener(e -> callback.onFailure(e));
            }
        }).addOnFailureListener(e -> callback.onFailure(e));
    }

    public interface ImageUrlCallback {
        void onSuccess(List<String> urls);
        void onFailure(Exception e);
    }

}
