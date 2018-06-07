package com.sinohb.system.upgrade.net.okhttp;

import java.io.File;
import java.util.Map;

import okhttp3.Callback;

public interface IOkhttpManager {

    public void doGet(String url, Callback callback);

    public void doPost(String url, Map<String, String> params, Callback callback);

    public void uploadPic(String url, File file, String fileName);

    public  void doPostJson(String url, String jsonParams, Callback callback);
}
