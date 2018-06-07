package com.sinohb.system.upgrade.net.retrofit;


import com.sinohb.system.upgrade.entity.Test;
import com.sinohb.system.upgrade.entity.UpgradeInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IUpgradeApi {
    @GET()
    Call<UpgradeInfo> getUpgradeInfo();

    @GET("/api/latest")
    Call<Test> getFixrate(@Query("access_key") String access_key, @Query("format") String format);

    @GET("/api")
    Call<Test> getFixrate();
}
