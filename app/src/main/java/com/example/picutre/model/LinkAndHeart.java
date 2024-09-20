package com.example.picutre.model;

public class LinkAndHeart {
    private String imageUrl;
    private boolean heart;

    public LinkAndHeart(String imageUrl, boolean heart) {
        this.imageUrl = imageUrl;
        this.heart = heart;
    }

    public LinkAndHeart() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isHeart() {
        return heart;
    }

    public void setHeart(boolean heart) {
        this.heart = heart;
    }
}
