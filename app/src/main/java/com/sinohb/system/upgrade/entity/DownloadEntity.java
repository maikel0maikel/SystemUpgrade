package com.sinohb.system.upgrade.entity;

public class DownloadEntity {
    private long doneSize;

    private String mFileName;//文件名

    private String mUrl;//下载地址

    private long mFileSize;//文件大小

    private int mThreadId;

    private long downloadStartIndex ;

    private long downloadEndIndex;


    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public long getmFileSize() {
        return mFileSize;
    }

    public void setmFileSize(long mFileSize) {
        this.mFileSize = mFileSize;
    }

    public int getThreadId() {
        return mThreadId;
    }

    public void setThreadId(int mThreadId) {
        this.mThreadId = mThreadId;
    }

    public long getDownloadStartIndex() {
        return downloadStartIndex;
    }

    public void setDownloadStartIndex(long downloadStartIndex) {
        this.downloadStartIndex = downloadStartIndex;
    }

    public long getDownloadEndIndex() {
        return downloadEndIndex;
    }

    public void setDownloadEndIndex(long downloadEndIndex) {
        this.downloadEndIndex = downloadEndIndex;
    }

    public long getDoneSize() {
        return doneSize;
    }

    public void setDoneSize(long doneSize) {
        this.doneSize = doneSize;
    }
}
