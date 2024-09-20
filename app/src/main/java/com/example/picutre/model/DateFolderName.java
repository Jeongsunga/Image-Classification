package com.example.picutre.model;

public class DateFolderName {
    int periodNumber;
    String folderName;
    String innoDate;

    public DateFolderName(int periodNumber, String folderName, String innoDate) {
        this.periodNumber = periodNumber;
        this.folderName = folderName;
        this.innoDate = innoDate;
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

    public String getInnoDate() {
        return innoDate;
    }

    public void setInnoDate(String innoDate) {
        this.innoDate = innoDate;
    }
}
