package com.sinohb.system.upgrade.task;

public interface DownloadListener {

    void onFinish();

    void onFailure(String error);

    void onProgress(int progress);

    void onFileSize(long size);

    void onFileName(String name);

    void onTaskComplete();

    void onTaskCancled();
}
