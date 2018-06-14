package com.sinohb.system.upgrade.downloader;

import com.sinohb.system.upgrade.downloader.task.BaseDownloadTask;

public interface DownloadProcessListener {
    void onDoneSize(long size);
    void onTaskFinished(BaseDownloadTask downloader);
    void onTaskCancel(BaseDownloadTask downloader);
    void onTaskFailure(BaseDownloadTask task,String error);
    void onTaskStoped(BaseDownloadTask task);
}
