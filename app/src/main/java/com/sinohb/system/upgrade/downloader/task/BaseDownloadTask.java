package com.sinohb.system.upgrade.downloader.task;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.database.DatabaseFoctory;
import com.sinohb.system.upgrade.downloader.DownloadProcessListener;
import com.sinohb.system.upgrade.entity.DownloadEntity;

import java.io.File;

public abstract class BaseDownloadTask implements Runnable {
    protected static final String TAG = BaseDownloadTask.class.getClass().getSimpleName();
    protected int mTheadId;
    protected File mFile;
    protected long downloadSize;
    protected String url;
    protected boolean isPause = false;
    protected boolean isCancel = false;
    protected Object sync = new Object();
    protected boolean isFinish = false;
    protected boolean stop = false;
    protected DownloadProcessListener mProcessListener;
    private long fileSize;

    public BaseDownloadTask(String url, File file, long downloadSize, int id, DownloadProcessListener processListener, long fileSize) {
        this.mFile = file;
        this.downloadSize = downloadSize;
        this.mTheadId = id;
        this.url = url;
        this.mProcessListener = processListener;
        this.fileSize = fileSize;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        synchronized (sync) {
            isPause = pause;
            sync.notifyAll();
        }

    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isStop() {
        return stop;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    protected synchronized void update(long size) {
        if (mProcessListener != null) {
            mProcessListener.onDoneSize(size);
        }
    }

    protected DownloadEntity getDownloadStartAndEnd() {
        DownloadEntity info = DatabaseFoctory.getInstance().getDownloadInfo(url, mTheadId);//dao.query(url.toString(), id);//查询记录当中没有下完的任务
        long finishSize = 0;
        long startIndex = (mTheadId - 1) * downloadSize;// 开始位置 = 已下载量
        if (info != null) {
            finishSize = info.getDownloadStartIndex();
            info.setDoneSize(finishSize - startIndex);
        } else {
            info = new DownloadEntity();
            info.setThreadId(mTheadId);
            info.setmUrl(url);
            info.setDownloadStartIndex(0l);
            DatabaseFoctory.getInstance().insert(info);
            finishSize = startIndex;
            info.setDoneSize(0l);
        }
        if (finishSize == 0) {
            finishSize = startIndex;
        }
        long endIndex = downloadSize * mTheadId - 1;
        endIndex = endIndex < fileSize ?
                endIndex : fileSize;
        LogTools.p("BaseDownloadTask", "startIndex=[" + finishSize + "],endIndex=[" + endIndex + "]");
        info.setDownloadStartIndex(finishSize);
        info.setDownloadEndIndex(endIndex);
        return info;
    }

    protected void cancelDownload(DownloadEntity info) {
        info.setDownloadStartIndex(0l);
        DatabaseFoctory.getInstance().update(info);
        isFinish = true;
        LogTools.e(TAG, "取消下载");
        if (mProcessListener != null) {
            mProcessListener.onTaskCancel(this);
        }
    }

    protected void finishDownload() {
        isFinish = true;
        DatabaseFoctory.getInstance().delete(url, mTheadId);
        LogTools.e(TAG, "下载任务完成：" + Thread.currentThread().getName());
        if (mProcessListener != null) {
            mProcessListener.onTaskFinished(this);
        }
    }

//    protected void downloadFailure(String error) {
//        DatabaseFoctory.getInstance().delete(url, mTheadId);
//        LogTools.e(TAG, "下载任务失败：" + Thread.currentThread().getName());
//        downloadNetWorkError(error);
//    }
    protected void updateProgress(DownloadEntity info, long finishSize) {
        info.setDownloadStartIndex(finishSize);
        DatabaseFoctory.getInstance().update(info);
    }
    protected void downloadNetWorkError(String error) {
        if (mProcessListener != null) {
            mProcessListener.onTaskFailure(error);
        }
        reset();
    }

    protected void reset(){
        isPause = false;
        isCancel = false;
        isFinish = false;
        stop = false;
    }
}
