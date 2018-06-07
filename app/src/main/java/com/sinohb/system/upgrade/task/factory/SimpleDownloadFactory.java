package com.sinohb.system.upgrade.task.factory;

public class SimpleDownloadFactory {
    private SimpleDownloadFactory() {
    }

    public static DownloadTask createTask(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return new HttpDownloadTask(url);
        } else {
            return new FTPDownloadTask(url);
        }
    }
}
