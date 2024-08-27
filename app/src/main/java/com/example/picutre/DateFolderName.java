package com.example.picutre;

public class DateFolderName {
    int periodNumber;
    String folderName;

    public DateFolderName(int periodNumber, String folderName) {
        this.periodNumber = periodNumber;
        this.folderName = folderName;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
