package com.sinohb.system.upgrade;

public interface BaseView<T extends BasePresenter> {
    void start();

    void pause();

    void cancel();

    void setPresenter(T presenter);
}
