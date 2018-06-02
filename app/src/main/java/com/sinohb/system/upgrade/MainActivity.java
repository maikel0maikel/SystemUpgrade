package com.sinohb.system.upgrade;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.presenter.DownloadController;
import com.sinohb.system.upgrade.presenter.DownloadPresenter;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements DownloadPresenter.View {
    private DownloadPresenter.Controller presenter;
    private TextView nameTv;
    private TextView sizeTv;
    private ProgressBar progressBar;
    private TextView downloadTv, pauseTv, resumeTV, cancelTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        } else {
            setContentView(R.layout.activity_main);
//        RecoverySystem.verifyPackage();
//        RecoverySystem.installPackage();
            initView();
            new DownloadController(this);
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.log";
        File file = new File(path);
        if (!file.exists()){
            try {
              boolean create =   file.createNewFile();
                LogTools.e("MainActivity","create:"+create);
            } catch (IOException e) {
                LogTools.e("MainActivity","create:"+e.getMessage());
            }
        }

    }

    private void initView() {
        nameTv = findViewById(R.id.fileNameTv);
        sizeTv = findViewById(R.id.fileSizeTv);
        progressBar = findViewById(R.id.progressBar);
        downloadTv = findViewById(R.id.downLoadBt);
        pauseTv = findViewById(R.id.pauseBt);
        cancelTv = findViewById(R.id.cancelBt);
        resumeTV = findViewById(R.id.resumeBt);
        pauseTv.setEnabled(false);
        cancelTv.setEnabled(false);
        downloadTv.setEnabled(true);
        resumeTV.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            finish();
        } else {
            setContentView(R.layout.activity_main);
            initView();
//        RecoverySystem.verifyPackage();
//        RecoverySystem.installPackage();
            new DownloadController(this);
        }
    }

    @Override
    public void downloadProcess(int process) {
        progressBar.setProgress(process);
    }

    @Override
    public void complete() {
        reset();
    }

    @Override
    public void failure() {

    }

    @Override
    public void notifyFileName(String fileName) {
        String text = nameTv.getText().toString();
        nameTv.setText(text + fileName);
    }

    @Override
    public void notifyFileSize(float size) {
        String text = sizeTv.getText().toString();
        sizeTv.setText(text + size + "M");
    }

    @Override
    public void notifyTaskCanceled() {
        reset();
    }

    @Override
    public void start() {
        presenter.start();
    }

    @Override
    public void pause() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void setPresenter(DownloadPresenter.Controller presenter) {
        this.presenter = presenter;
        this.presenter.start();
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.downLoadBt:
                this.presenter.download("http://downloadz.dewmobile.net/Official/Kuaiya482.apk");
                pauseTv.setEnabled(true);
                cancelTv.setEnabled(true);
                resumeTV.setEnabled(false);
                break;
            case R.id.pauseBt:
//                downloadTv.setEnabled(true);
                resumeTV.setEnabled(true);
                cancelTv.setEnabled(false);
                presenter.pause();
                break;
            case R.id.resumeBt:
                presenter.resume();
                cancelTv.setEnabled(true);
                pauseTv.setEnabled(true);
                break;
            case R.id.cancelBt:
                presenter.cancel();
                break;
        }
        view.setEnabled(false);
    }
    private void reset(){
        progressBar.setProgress(0);
        progressBar.invalidate();
        downloadTv.setEnabled(true);
        pauseTv.setEnabled(false);
        nameTv.setText("文件名：");
        sizeTv.setText("文件大小：");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThreadPool.getPool().destroy();
    }
}
