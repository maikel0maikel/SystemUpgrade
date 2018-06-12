package com.sinohb.system.upgrade.downloader;

import com.sinohb.system.upgrade.entity.UpgradeEntity;

public interface DownloadListener {

    void onFinish(String downloadFilePath);

    void onFailure(String error);

    void onProgress(int progress);

    //void onFileSize(long size);

    //void onFileName(String name);

    void onUpgradeInfo(UpgradeEntity upgradeEntity);

    void onTaskComplete();

    void onTaskCancled();

    void onNoNewVersion();
}
