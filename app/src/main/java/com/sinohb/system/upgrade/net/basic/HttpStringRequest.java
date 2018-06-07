package com.sinohb.system.upgrade.net.basic;

/**
 * Created by maikel on 2018/3/31.
 */

public class HttpStringRequest extends HttpRequest {
    public HttpStringRequest(String url) {
        super(url);
    }


    @Override
    protected BaseTask buildTask() {
        return new HttpStringTask(this);
    }
}
