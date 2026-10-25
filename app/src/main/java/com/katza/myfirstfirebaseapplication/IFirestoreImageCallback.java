package com.katza.myfirstfirebaseapplication;

public interface IFirestoreImageCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);

}
