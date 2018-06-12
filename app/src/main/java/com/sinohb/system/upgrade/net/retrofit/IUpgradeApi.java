package com.sinohb.system.upgrade.net.retrofit;


import com.sinohb.system.upgrade.entity.UpgradeEntity;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IUpgradeApi {
    @GET()
    Call<UpgradeEntity> getUpgradeInfo();

//    @GET("/api/latest")
//    Call<Test> getFixrate(@Query("access_key") String access_key, @Query("format") String format);
//
//    @GET("/api")
//    Call<Test> getFixrate();
}
