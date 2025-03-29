package com.ordering.blinkit_clone.http;

public interface ApiCallback<T> {
    void onSuccess(T response);
    void onError(String error);
}
