package com.sinohb.system.upgrade.downloader.manager;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.downloader.task.BaseDownloadTask;
import com.sinohb.system.upgrade.entity.UpgradeEntity;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.downloader.DownloadListener;
import com.sinohb.system.upgrade.downloader.DownloadProcessListener;
import com.sinohb.system.upgrade.downloader.task.FTPDownloadTask;
import com.sinohb.system.upgrade.downloader.task.HttpDownloadTask;
import com.sinohb.system.upgrade.utils.VertifyUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseDownloadManager implements Callable<UpgradeEntity>, DownloadProcessListener {
    protected static final String TAG = BaseDownloadManager.class.getSimpleName();
    private static final int DOWN_LOAD_COUNT = 5;
    protected long mFileSize = 0;
    private CopyOnWriteArrayList<Runnable> downloaders;
    private volatile long mFinishSize = 0;
    protected String url;
    private DownloadListener mListener;
    private String mDownloadFilePath;

    public abstract Object connect(String url) throws IOException;

    public BaseDownloadManager(String url) {
        this.url = url;
        downloaders = new CopyOnWriteArrayList<>();
    }

    public void setListener(DownloadListener listener) {
        this.mListener = listener;
    }


    protected void downloadFailure(String error) {
        if (mListener != null) {
            mListener.onFailure(error);
        }
    }

    protected void noNewVersion(){
        if (mListener != null) {
            mListener.onNoNewVersion();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public synchronized void onDoneSize(long size) {
        if (mFinishSize < 0) {
            mFinishSize = 0;
        }
        mFinishSize += size;
        int progress = (int) (100 * mFinishSize / mFileSize);
        if (mListener != null) {
            mListener.onProgress(progress);
        }
        if (mFinishSize >= mFileSize) {
            if (mListener != null) {
                mListener.onFinish(mDownloadFilePath);
                reset();
            }
        }
    }

    @Override
    public void onTaskFinished(BaseDownloadTask downloader) {
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
    public void onTaskCancel(BaseDownloadTask downloader) {
        downloaders.remove(downloader);
        if (downloaders.isEmpty()) {
            LogTools.e(TAG, "all task is executed");
            if (mListener != null) {
                mListener.onTaskCancled();
                reset();
            }
        }
    }

    @Override
    public void onTaskFailure(String error) {
        stop();
        downloaders.clear();
        if (mListener != null) {
            mListener.onFailure(error);
            reset();
        }
    }

    public void pause() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                BaseDownloadTask downloaderTask = (BaseDownloadTask) task;
                if (!downloaderTask.isFinish()) {
                    downloaderTask.setPause(true);
                }
            }
        }
    }

    public void resume() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                BaseDownloadTask downloaderTask = (BaseDownloadTask) task;
                if (downloaderTask.isPause() && !downloaderTask.isFinish()) {
                    downloaderTask.setPause(false);
                }
            }
        }
    }

    public void stop() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                BaseDownloadTask downloaderTask = (BaseDownloadTask) task;
                if (!downloaderTask.isFinish()) {
                    downloaderTask.setStop(true);
                } else {//保存已经完成的下载线程.

                }
            }
        }
    }

    public void cancel() {
        if (downloaders != null) {
            for (Runnable task : downloaders) {
                BaseDownloadTask downloaderTask = (BaseDownloadTask) task;
                downloaderTask.setCancel(true);
            }
        }
    }

    private synchronized void reset() {
        mFinishSize = 0;
        mFileSize = 0;
        if (downloaders != null) {
            downloaders.clear();
        }
    }

    protected void startDownload(String url, File mDownloadFile) throws IOException {
//        File mDownloadFile = new File(UpgradeConstants.DOWNLOAD_PATH, fileName);
//        if (!mDownloadFile.getParentFile().exists()) {
//            mDownloadFile.getParentFile().mkdirs();
//        }
        mDownloadFilePath = mDownloadFile.getAbsolutePath();
        LogTools.p(TAG, "下载路径：" + mDownloadFilePath);
        RandomAccessFile raf = new RandomAccessFile(mDownloadFile, "rws");
        raf.setLength(mFileSize);
        raf.close();
        long partLen = mFileSize % DOWN_LOAD_COUNT == 0 ? (mFileSize / DOWN_LOAD_COUNT) : (mFileSize / DOWN_LOAD_COUNT + 1);//每个线程下载的大小
        LogTools.p(TAG, "partLen:" + partLen);
        for (int i = 1; i <= DOWN_LOAD_COUNT; i++) {
            BaseDownloadTask downloader;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                downloader = new HttpDownloadTask(url, mDownloadFile, partLen, i, this, mFileSize);
            } else {
                downloader = new FTPDownloadTask(url, mDownloadFile, partLen, i, this, mFileSize);
            }
            downloaders.add(downloader);
        }
        mFinishSize = 0;
        ThreadPool.getPool().execute(downloaders);
    }

}
