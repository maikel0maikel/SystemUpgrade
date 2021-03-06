package com.sinohb.system.upgrade.downloader.manager;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.entity.DownloadEntity;
import com.sinohb.system.upgrade.entity.UpgradeEntity;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloadManager extends BaseDownloadManager {
    private static final String TAG = "HttpDownloadManager";

    public HttpDownloadManager(String url) {
        super(url);
    }

    @Override
    public UpgradeEntity call() {
        HttpURLConnection connection = null;
        UpgradeEntity upgradeEntity = null;
        try {
            connection = (HttpURLConnection) connect(url);
            int code = connection.getResponseCode();
            if (code == 200) {
                upgradeEntity = new UpgradeEntity();
                long fileSize = connection.getContentLength();
                mFileSize = fileSize;
                upgradeEntity.setFileSize(fileSize);
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                upgradeEntity.setFileName(fileName);
                File mDownloadFile = new File(UpgradeConstants.DOWNLOAD_PATH, upgradeEntity.getFileName());
                if (!mDownloadFile.getParentFile().exists()) {
                    mDownloadFile.getParentFile().mkdirs();
                }
                notifyUpdateInfo(upgradeEntity);
                if (mDownloadFile.exists()&&mDownloadFile.length()==mFileSize){
//                    String localMd5 = null;
//                    try {
//                        localMd5 = VertifyUtils.getMD5(mDownloadFile.getAbsolutePath());
//
//                    } catch (NoSuchAlgorithmException e) {
//                        LogTools.p(TAG,e,"get md5 error ");
//                    }
//                    if (localMd5 != null && localMd5.length() > 0 && localMd5.equalsIgnoreCase(upgradeEntity.getMD5())) {
//                        LogTools.p(TAG,"本地存在要下载的文件并且md5校验通过不再下载");
//
//                        return upgradeEntity;
//                    }
                    onDoneSize(mDownloadFile.length());
                    LogTools.p(TAG,"文件存在不下载");
                    return upgradeEntity;
                }else {
                    LogTools.p(TAG,"本地文件存在md5校验不通过或者文件大小为0需下载");
                }
                startDownload(url,mDownloadFile);
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
        return upgradeEntity;
    }

    @Override
    public Object connect(String url) throws IOException {
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

    @Override
    public UpgradeEntity getDownloadInfo() {
        return null;
    }
}
