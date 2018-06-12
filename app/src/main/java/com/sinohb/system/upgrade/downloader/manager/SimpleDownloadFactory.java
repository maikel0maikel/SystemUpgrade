package com.sinohb.system.upgrade.downloader.manager;


public class SimpleDownloadFactory {
    private SimpleDownloadFactory() {
    }

    public static BaseDownloadManager createTask(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return new HttpDownloadManager(url);
        } else {
            return new FTPDownloadManager(url);
        }
    }

}
