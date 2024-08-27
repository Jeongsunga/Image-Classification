package com.example.picutre;

// 사용자의 스마트폰 갤러리 정보들을 저장하는 클래스
public class FolderItem {
    private String folderName; //폴더 이름
    private String firstImagePath; //사진 경로로 저장
    private int count;

    public FolderItem(String folderName, String firstImagePath, int count) {
        this.folderName =  folderName;
        this.firstImagePath = firstImagePath;
        this.count = count;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String name) {
        this.folderName = folderName;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}