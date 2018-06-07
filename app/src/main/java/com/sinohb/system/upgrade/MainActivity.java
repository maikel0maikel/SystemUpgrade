package com.sinohb.system.upgrade;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sinohb.logger.LogTools;
import com.sinohb.system.upgrade.entity.Test;
import com.sinohb.system.upgrade.net.basic.HttpRequest;
import com.sinohb.system.upgrade.net.basic.HttpStringRequest;
import com.sinohb.system.upgrade.net.okhttp.OkhttpFactory;
import com.sinohb.system.upgrade.net.retrofit.RetrofitFactory;
import com.sinohb.system.upgrade.pool.ThreadPool;
import com.sinohb.system.upgrade.presenter.DownloadController;
import com.sinohb.system.upgrade.presenter.DownloadPresenter;
import com.sinohb.system.upgrade.service.DownloadService;
import com.sinohb.system.upgrade.view.UpgradeDialog;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements DownloadPresenter.View {
    private DownloadPresenter.Controller presenter;
    private TextView nameTv;
    private TextView sizeTv;
    private ProgressBar progressBar;
    private TextView downloadTv, pauseTv, resumeTV, cancelTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
//        RecoverySystem.verifyPackage();
//        RecoverySystem.installPackage();
            initView();
            new DownloadController(this);
        startService(new Intent(this, DownloadService.class));
    }

    private void initView() {
        nameTv = (TextView) findViewById(R.id.fileNameTv);
        sizeTv = (TextView) findViewById(R.id.fileSizeTv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        downloadTv = (TextView) findViewById(R.id.downLoadBt);
        pauseTv = (TextView) findViewById(R.id.pauseBt);
        cancelTv = (TextView) findViewById(R.id.cancelBt);
        resumeTV = (TextView) findViewById(R.id.resumeBt);
        pauseTv.setEnabled(false);
        cancelTv.setEnabled(false);
        downloadTv.setEnabled(true);
        resumeTV.setEnabled(false);
    }


    @Override
    public void downloadProcess(int process) {
        progressBar.setProgress(process);
    }

    @Override
    public void complete() {
        reset();
//        startActivity(new Intent(this, UpgradeActivity.class));
        new UpgradeDialog(this).show();
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
                //sendRequest();
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
                //presenter.cancel();
                sendRequest();
                break;
        }
        view.setEnabled(false);
    }
    private void sendRequest() {
//        HttpStringRequest request = new HttpStringRequest("http://data.fixer.io/api/latest?");
//        Map<String ,String> pa = new HashMap<>();
//        pa.put("access_key","a791f2706839b110dee4dd7f5d2c947c");
//        request.setParamsMap(pa);
//        request.setResponseListener(new HttpRequest.ResponseListener<String>() {
//            @Override
//            public void onSuccess(String response) {
//                LogTools.d("sendRequest", "response:" + response);
//            }
//
//            @Override
//            public void onFailure(String error) {
//                LogTools.d("sendRequest", "error:" + error);
//            }
//        }).setResponseOnMainThread(true).submitRequest();
        OkhttpFactory.getFactory().doGet("http://data.fixer.io/api/latest?access_key=a791f2706839b110dee4dd7f5d2c947c&format=1", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogTools.d("sendRequest", "error:" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogTools.d("sendRequest", "response:" + response.body().string());
            }
        });
//        RetrofitFactory.getFactory().getFixrate("a791f2706839b110dee4dd7f5d2c947c",new retrofit2.Callback<Test> () {
//            @Override
//            public void onResponse(retrofit2.Call<Test> call, retrofit2.Response<Test> response) {
//                LogTools.d("sendRequest", "response:" + response.body().getBase());
//            }
//
//            @Override
//            public void onFailure(retrofit2.Call<Test> call, Throwable t) {
//                LogTools.d("sendRequest", "error:" + t.getMessage());
//            }
//
//        });
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
       presenter.onDestroy();
    }
}
