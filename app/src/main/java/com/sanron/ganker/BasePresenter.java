package com.sanron.ganker;

/**
 * Created by sanron on 16-7-6.
 */
public interface BasePresenter<T> {
    void attach(T view);
}
