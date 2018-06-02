package com.sinohb.system.upgrade.task;

import android.os.Environment;

import com.sinohb.logger.LogTools;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask implements Runnable, Downloader.DownloadProcessListener {
    private static final String TAG = "DownloadTask";
    private static final int DOWN_LOAD_COUNT = 5;
    private String url;
    private DownloadListener mListener;
    private volatile long mFinishSize = 0;
    private long mFileSize = 0;

    public DownloadTask(String url) {
        this.url = url;
    }

    public void setListener(DownloadListener listener) {
        this.mListener = listener;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            URL downloadUrl = new URL(url);
            connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            int code = connection.getResponseCode();
            if (code == 200) {
                long fileSize = connection.getContentLength();
                mFileSize = fileSize;
                if (mListener != null) {
                    mListener.onFileSize(fileSize);
                }
                String name = url.substring(url.lastIndexOf("/") + 1);
                if (mListener != null) {
                    mListener.onFileName(name);
                }
                File file = new File(Environment.getExternalStorageDirectory(), name);
                RandomAccessFile raf = new RandomAccessFile(file, "rws");
                raf.setLength(fileSize);
                raf.close();
//                long partLen = (fileSize + DOWN_LOAD_COUNT - 1) / DOWN_LOAD_COUNT;//每个线程下载的大小
                long partLen = fileSize % DOWN_LOAD_COUNT == 0 ? (fileSize / DOWN_LOAD_COUNT) : (fileSize / DOWN_LOAD_COUNT + 1);
                for (int i = 1; i <= DOWN_LOAD_COUNT; i++) {
                    new Thread(new Downloader(url, file, partLen, i, this)).start();
                }
            }
        } catch (MalformedURLException e) {
            String error = e.getMessage() == null ? "unknown error " : e.getMessage();
            LogTools.e(TAG, "download failure error=" + error);
            downloadFailure(error);
        } catch (IOException e) {
            String error = e.getMessage() == null ? "unknown error " : e.getMessage();
            downloadFailure(error);
            LogTools.e(TAG, "download failure error=" + error);

        } finally {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
    }

    private void downloadFailure(String error) {
        if (mListener != null) {
            mListener.onFailure(error);
        }
    }


    @Override
    public synchronized void onDoneSize(long size) {
        LogTools.e(TAG, "mFinishSize:" + mFinishSize+",size:"+size);
        mFinishSize += size;
        LogTools.e(TAG, "mFinishSize:" + mFinishSize+",mFileSize:"+mFileSize);
        int progress = (int) (100*mFinishSize / mFileSize);
        if (mListener != null) {
            mListener.onProgress(progress);
        }
        if (mFinishSize == mFileSize){
            if (mListener!=null){
                mListener.onFinish();
            }
        }
    }
}
