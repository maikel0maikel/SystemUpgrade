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
        downloadTask = new DownloadTask("http://downloadz.dewmobile.net/Official/Kuaiya482.apk");
        downloadTask.setListener(this);
        ThreadPool.getPool().execute(downloadTask);
    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void onFinish() {
        LogTools.e(TAG, "onFinish");
    }

    @Override
    public void onFailure(String error) {
        LogTools.e(TAG, "onFailure:" + error);
    }

    @Override
    public synchronized void onProgress(int progress) {
        LogTools.i(TAG, "onProgress:" + progress);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_PROGRESS,progress).sendToTarget();
    }

    @Override
    public void onFileSize(long size) {
        float fileSize = size / (1024.0f * 1024.0f);
        LogTools.e(TAG, "fileSize:" + fileSize);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_SIZE,fileSize).sendToTarget();
    }

    @Override
    public void onFileName(String name) {
        LogTools.e(TAG, "onFileName:" + name);
        mHandler.obtainMessage(UpdateHandler.MSG_FILE_NAME,name).sendToTarget();
    }

    private static class UpdateHandler extends Handler {
        static final int MSG_FILE_NAME = 1;
        static final int MSG_FILE_SIZE = 2;
        static final int MSG_FILE_PROGRESS = 3;
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
            }
        }
    }
}
