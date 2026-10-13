package com.katza.myfirstfirebaseapplication;

import java.util.ArrayList;

public interface IFirestoreCallback {
    void onDataReceived(ArrayList<Student> students);
}
