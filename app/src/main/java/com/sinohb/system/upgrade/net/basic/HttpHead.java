package com.sinohb.system.upgrade.net.basic;

/**
 * Created by maikel on 2018/3/31.
 */

public class HttpHead {
    private String key;
    private String value;
    public HttpHead(){

    }
    public HttpHead(String key, String value){
        this.key = key;
        this.value = value;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
