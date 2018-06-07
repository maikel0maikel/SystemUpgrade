package com.sinohb.system.upgrade.net.basic;


/**
 * Created by maikel on 2018/3/31.
 */

public interface IHttpManager {

    void submmitRequest(HttpRequest request);

    void shutdown();

    void shutdownAll();
}
