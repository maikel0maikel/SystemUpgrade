package com.sinohb.system.upgrade.net.basic;

/**
 * Created by maikel on 2018/3/31.
 */

public class HttpResponse<T> {
    protected int code;
    protected String error;
    T response;
    public HttpResponse(){

    }
    public HttpResponse(T response, int code, String error){
        this.code = code;
        this.error = error;
        this.response = response;
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError(){
        return error;
    }
    public void setError(String error){
        this.error = error;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
