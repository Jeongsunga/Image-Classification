package com.example.picutre;

public class DataModel {
    private String value;
    private int age;

    public DataModel(String value, int age) {
        this.age = age;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
