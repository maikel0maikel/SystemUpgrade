package com.sinohb.system.upgrade.task.factory;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.constant.UpgradeConstants;
import com.sinohb.system.upgrade.entity.DownloadInfo;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.task.BaseDownloader;
import com.sinohb.system.upgrade.task.DownloadListener;
import com.sinohb.system.upgrade.task.DownloadProcessListener;
import com.sinohb.system.upgrade.task.Downloader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class DownloadTask implements Callable<DownloadInfo>, DownloadProcessListener {
    private static final String TAG = "DownloadTask";
    private static final int DOWN_LOAD_COUNT = 5;
    protected long mFileSize = 0;
    private List<Runnable> downloaders;
    private volatile long mFinishSize = 0;
    protected String url;
    private DownloadListener mListener;
    protected String fileName = "";

    public abstract Object connect() throws IOException;

    public DownloadTask(String url) {
        this.url = url;
        downloaders = new ArrayList<>();
    }

    public void setListener(DownloadListener listener) {
        this.mListener = listener;
    }


    protected void downloadFailure(String error) {
        if (mListener != null) {
            mListener.onFailure(error);
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

    private void reset() {
        mFinishSize = 0;
        mFileSize = 0;
    }

    protected void startDownload() throws IOException {
        File file = new File(UpgradeConstants.DOWNLOAD_PATH, fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        LogTools.p(TAG, "下载路径：" + file.getAbsolutePath());
        RandomAccessFile raf = new RandomAccessFile(file, "rws");
        raf.setLength(mFileSize);
        raf.close();
        long partLen = mFileSize % DOWN_LOAD_COUNT == 0 ? (mFileSize / DOWN_LOAD_COUNT) : (mFileSize / DOWN_LOAD_COUNT + 1);//每个线程下载的大小

        for (int i = 1; i <= DOWN_LOAD_COUNT; i++) {
            Downloader downloader = new Downloader(url, file, partLen, i, this);
            downloaders.add(downloader);
        }
        ThreadPool.getPool().execute(downloaders);
    }

}
