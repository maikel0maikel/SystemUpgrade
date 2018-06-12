package com.sinohb.system.upgrade.downloader.task;

import com.sinohb.system.upgrade.utils.VertifyUtils;

import java.util.concurrent.Callable;

public class VertifyTask implements Callable<String>{
    private String mVertifyFilePath;
    public VertifyTask(String filePath){
        mVertifyFilePath = filePath;
    }
    @Override
    public String call() throws Exception {
        return VertifyUtils.getMD5(mVertifyFilePath);
    }
}
