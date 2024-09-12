package com.example.picutre;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>{

    private List<String> imageUrls;
    private Context context;
    private boolean isImageOne;
    private String metadataList;
    private ImageUrlApi imageUrlApi;
    private DeleteImageApi deleteImageApi;
    private DownloadImage downloadImage;
    String infoes;
    private OnItemClickListener listener;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://172.21.223.102:5000/")  // 로컬 호스트 주소
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public ImageSliderAdapter(List<String> imageUrls, Context context, String metadataList, OnItemClickListener listener) {
        this.imageUrls = imageUrls;
        this.context = context;
        this.metadataList = metadataList;
        this.listener = listener;
    }

    public void updateMetadata(String metadataList) {
        this.metadataList = metadataList;
        notifyDataSetChanged(); // Adapter에 데이터가 변경되었음을 알림
    }

//    public void updateRefImageUrl(String oneImageUrl) {
//        this.oneImageUrl = oneImageUrl;
//        notifyDataSetChanged();
//    }

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
                .placeholder(R.drawable.clover)
                .error(R.drawable.nwh28)
                .into(holder.imageView);

        holder.btn_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (isImageOne) {
                        holder.btn_heart.setImageResource(R.drawable.fullheart); // 변경할 이미지
                    } else {
                        holder.btn_heart.setImageResource(R.drawable.heart); // 원래 이미지
                    }
                    // 상태 토글
                    isImageOne = !isImageOne;
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

    public void showDownloadDialog(String imageUrl) {
        new AlertDialog.Builder(context)
                .setTitle("다운로드")
                .setMessage("이 사진을 갤러리에 저장하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Log.d(TAG, "Download Link : " + imageUrls.get(position));
                       // downloadAndSaveImage(imageUrl); // 서버의 이미지 URL 경로
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 아무 동작 수행 X
                    }
                }).show();
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

    private void downloadAndSaveImage(String imagePath) {

        downloadImage = retrofit.create(DownloadImage.class);
        Call<ResponseBody> call = downloadImage.downloadImage(imagePath);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new Thread(() -> {
                        try {
                            // 서버에서 이미지 다운로드
                            ResponseBody body = response.body();
                            Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());

                            // 외부 저장소에 이미지 저장
                            File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File file = new File(externalStorageDir, "downloaded_image.jpg");

                            try (OutputStream output = new FileOutputStream(file)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                            }

                            // 미디어 스캐너에 이미지 추가 알림
                            //MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), null);

                            // 갤러리 업데이트
                            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                            // UI 스레드에서 Toast 메시지
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show());

                        } catch (IOException e) {
                            e.printStackTrace();
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                //new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(MainActivity.this, "Failed to download image", Toast.LENGTH_SHORT).show());
            }
        });
    }

}
