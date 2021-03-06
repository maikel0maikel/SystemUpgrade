package com.sinohb.system.upgrade.task;


import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.pool.ThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadTask implements Runnable, DownloadProcessListener {
    private static final String TAG = "DownloadTask";
    private static final int DOWN_LOAD_COUNT = 5;
    private String url;
    private DownloadListener mListener;
    private volatile long mFinishSize = 0;
    private long mFileSize = 0;
    private List<Runnable> downloaders;
    public DownloadTask() {

    }

    public DownloadTask(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setListener(DownloadListener listener) {
        this.mListener = listener;
    }

    @Override
    public void run() {
        if (url == null || url.length() == 0) {
            if (mListener != null) {
                mListener.onFailure("empty url please check");
            }
            return;
        }
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
                File file = new File(UpgradeConstants.DOWNLOAD_PATH, name);
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
               String filePath = file.getAbsolutePath();
                LogTools.p(TAG,"下载路径："+filePath);
                if (mListener != null) {
                    mListener.onFileName(name);
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rws");
                raf.setLength(fileSize);
                raf.close();
                long partLen = fileSize % DOWN_LOAD_COUNT == 0 ? (fileSize / DOWN_LOAD_COUNT) : (fileSize / DOWN_LOAD_COUNT + 1);//每个线程下载的大小
                downloaders = new ArrayList<>();
                for (int i = 1; i <= DOWN_LOAD_COUNT; i++) {
                    Downloader downloader
                            = new Downloader(url, file, partLen, i, this);
                    // new Thread(downloaders[j]).start();
                    downloaders.add(downloader);
                }
                ThreadPool.getPool().execute(downloaders);
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
        LogTools.i(TAG, "mFinishSize:" + mFinishSize + ",size:" + size);
        mFinishSize += size;
        LogTools.i(TAG, "mFinishSize:" + mFinishSize + ",mFileSize:" + mFileSize);
        int progress = (int) (100 * mFinishSize / mFileSize);
        if (mListener != null) {
            mListener.onProgress(progress);
        }
        if (mFinishSize == mFileSize) {
            if (mListener != null) {
                mListener.onFinish();
                reset();
            }
        }
    }

    @Override
    public void onTaskFinished(Downloader downloader) {
        downloaders.remove(downloader);
        if (downloaders.isEmpty()) {
            LogTools.e(TAG, "all task is executed");
            if (mListener != null) {
                mListener.onTaskComplete();
                reset();
            }
        }
    }

    @Override
    public void onTaskCancel(BaseDownloader downloader) {
        downloaders.remove(downloader);
        if (downloaders.isEmpty()) {
            LogTools.e(TAG, "all task is executed");
            if (mListener != null) {
                mListener.onTaskCancled();
                reset();
            }
        }
    }

    public void pause() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                Downloader downloaderTask = (Downloader) task;
                if (!downloaderTask.isFinish()) {
                    downloaderTask.setPause(true);
                }
            }
        }
    }

    public void resume() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                Downloader downloaderTask = (Downloader) task;
                if (downloaderTask.isPause() && !downloaderTask.isFinish()) {
                    downloaderTask.setPause(false);
                }
            }
        }
    }

    public void cancel() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                Downloader downloaderTask = (Downloader) task;
                downloaderTask.setCancel(true);
            }
        }
    }

    private void reset(){
        mFinishSize = 0;
        mFileSize = 0;
    }
}
