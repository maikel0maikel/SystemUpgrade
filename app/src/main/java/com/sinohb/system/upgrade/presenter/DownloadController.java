package com.sinohb.system.upgrade.presenter;

import android.os.Handler;
import android.os.Message;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.task.DownloadListener;
import com.sinohb.system.upgrade.task.DownloadTask;

import java.lang.ref.WeakReference;


public class DownloadController implements DownloadPresenter.Controller, DownloadListener {
    private static final String TAG = "DownloadController";
    private DownloadTask downloadTask;
    DownloadPresenter.View view;
    private UpdateHandler mHandler;

    public DownloadController(DownloadPresenter.View view) {
        this.view = view;
        mHandler = new UpdateHandler(this);
        this.view.setPresenter(this);
    }

    @Override
    public void start() {
        downloadTask = new DownloadTask();
        downloadTask.setListener(this);
    }

    @Override
    public void pause() {
        downloadTask.pause();
    }

    @Override
    public void resume() {
        downloadTask.resume();
    }

    @Override
    public void cancel() {
        downloadTask.cancel();
    }

    @Override
    public void download(String url) {
        downloadTask.setUrl(url);
        ThreadPool.getPool().execute(downloadTask);
    }

    @Override
    public void taskCanceled() {
        LogTools.e(TAG, "taskCanceled");
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_CANCELED);
    }

    @Override
    public void onFinish() {
        LogTools.e(TAG, "onFinish");
        mHandler.sendEmptyMessage(UpdateHandler.MSG_DOWNLOAD_COMPLETE);
    }

    @Override
    public void onFailure(String error) {
        LogTools.e(TAG, "onFailure:" + error);
    }

    @Override
    public synchronized void onProgress(int progress) {
        LogTools.i(TAG, "onProgress:" + progress);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_PROGRESS, progress).sendToTarget();
    }

    @Override
    public void onFileSize(long size) {
        float fileSize = size / (1024.0f * 1024.0f);
        LogTools.e(TAG, "fileSize:" + fileSize);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_SIZE, fileSize).sendToTarget();
    }

    @Override
    public void onFileName(String name) {
        LogTools.e(TAG, "onFileName:" + name);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_NAME, name).sendToTarget();
    }

    @Override
    public void onTaskComplete() {
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_FINISHED);
    }

    @Override
    public void onTaskCancled() {
        mHandler.sendEmptyMessage(UpdateHandler.MSG_TASK_CANCELED);
    }

    private static class UpdateHandler extends Handler {
        static final int MSG_FILE_NAME = 1;
        static final int MSG_FILE_SIZE = 2;
        static final int MSG_FILE_PROGRESS = 3;
        static final int MSG_TASK_CANCELED = 4;
        static final int MSG_TASK_FINISHED = 5;
        static final int MSG_DOWNLOAD_COMPLETE = 6;
        private WeakReference<DownloadController> controllerWeakReference;

        UpdateHandler(DownloadController controller) {
            controllerWeakReference = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (controllerWeakReference == null) {
                LogTools.e(TAG, "controllerWeakReference is null");
                return;
            }
            DownloadController controller = controllerWeakReference.get();
            if (controller == null) {
                LogTools.e(TAG, "controller is null");
                return;
            }
            switch (msg.what) {
                case MSG_FILE_NAME:
                    String fileName = (String) msg.obj;
                    controller.view.notifyFileName(fileName);
                    break;
                case MSG_FILE_SIZE:
                    float fileSize = (float) msg.obj;
                    controller.view.notifyFileSize(fileSize);
                    break;
                case MSG_FILE_PROGRESS:
                    int progress = (int) msg.obj;
                    controller.view.downloadProcess(progress);
                    break;
                case MSG_TASK_CANCELED:
                    controller.view.notifyTaskCanceled();
                    break;
                case MSG_TASK_FINISHED:
                    controller.view.notifyTaskCanceled();
                    break;
                case MSG_DOWNLOAD_COMPLETE:
                    controller.view.complete();
                    break;
            }
        }
    }
}
