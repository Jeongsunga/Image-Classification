package com.example.picutre.model;

import java.util.List;

public class DeleteResponse {
    private boolean success;
    private int image_count;
    private List<String> image_links;

    public boolean isSuccess() {
        return success;
    }

    public int getImageCount() {
        return image_count;
    }
    public List<String> getImageLinks() {
        return image_links;
    }
}
