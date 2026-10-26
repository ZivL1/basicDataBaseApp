package com.katza.myfirstfirebaseapplication;

public class Student {

    private String fullName, id, data;
    private int grade;

    public Student() {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Name: " + fullName  +
                ", " + id +
                ", grade: " + grade ;
    }
}
