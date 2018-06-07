package com.sinohb.system.upgrade.task.factory;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.entity.DownloadInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloadTask extends DownloadTask {
    private static final String TAG = "HttpDownloadTask";

    public HttpDownloadTask(String url) {
        super(url);
    }

    @Override
    public DownloadInfo call() {
        HttpURLConnection connection = null;
        DownloadInfo downloadInfo = null;
        try {
            connection = (HttpURLConnection) connect();
            int code = connection.getResponseCode();
            if (code == 200) {
                downloadInfo = new DownloadInfo();
                long fileSize = connection.getContentLength();
                mFileSize = fileSize;
                downloadInfo.setmFileSize(fileSize);
                fileName = url.substring(url.lastIndexOf("/") + 1);
                downloadInfo.setmFileName(fileName);
                startDownload();
            }
        } catch (IOException e) {
            String error = e.getMessage() == null ? "unknown error " : e.getMessage();
            LogTools.e(TAG, "download failure error=" + error);
            downloadFailure(error);
        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
        return downloadInfo;
    }

    @Override
    public   Object connect() throws IOException {
        if (url == null || url.length() == 0) {
            throw new IOException("url is null please check");
        }
        HttpURLConnection connection;
        URL downloadUrl = new URL(url);
        connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        return connection;
    }
}
