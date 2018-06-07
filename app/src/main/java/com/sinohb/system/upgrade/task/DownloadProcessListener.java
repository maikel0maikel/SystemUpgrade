package com.sinohb.system.upgrade.task;

public interface DownloadProcessListener {
    void onDoneSize(long size);
    void onTaskFinished(Downloader downloader);
    void onTaskCancel(BaseDownloader downloader);
}
