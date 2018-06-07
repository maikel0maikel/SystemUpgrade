package com.sinohb.system.upgrade.net.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sinohb.system.upgrade.entity.Test;
import com.sinohb.system.upgrade.entity.UpgradeInfo;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitApiImpl implements RetrofitApiable{

    IUpgradeApi upgradeApi;

    RetrofitApiImpl(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://data.fixer.io/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create(gson)) //设置使用Gson解析(记得加入依赖)
                .build();
        upgradeApi = retrofit.create(IUpgradeApi.class);
    }
    @Override
    public void getUpgradeInfo(Callback<UpgradeInfo> callback) {
       Call<UpgradeInfo> request = upgradeApi.getUpgradeInfo();
        request.enqueue(callback);
    }

    @Override
    public void getFixrate(String access_key,Callback<Test>  callback) {
        Call<Test>  request = upgradeApi.getFixrate(access_key,"1");
        request.enqueue(callback);
    }
}
