package com.example.picutre;

// Storage~~~ : 파이어베이스 스토리지의 폴더들을 사용자의 스마트폰으로 보여주는 것

public class StorageItem {
    private String folderName2;
    private String firstImagePath2;
    private int count2;

    public String getFolderName2() {
        return folderName2;
    }

    public void setFolderName2(String folderName2) {
        this.folderName2 = folderName2;
    }

    public String getFirstImagePath2() {
        return firstImagePath2;
    }

    public void setFirstImagePath2(String firstImagePath2) {
        this.firstImagePath2 = firstImagePath2;
    }

    public int getCount2() {
        return count2;
    }

    public void setCount2(int count2) {
        this.count2 = count2;
    }

    public StorageItem(String folderName2, String firstImagePath2, int count2) {
        this.folderName2 = folderName2;
        this.firstImagePath2 = firstImagePath2;
        this.count2 = count2;
    }
}

