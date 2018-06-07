package com.sinohb.system.upgrade.net.basic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * Created by maikel on 2018/3/31.
 */

public abstract class HttpRequest {
    private static final int HTTP_RESULT = 1;
    private ResponseListener listener;
    private boolean isResponseOnMainThread = false;
    private String url;
    private HttpHead mHead;
    private HttpMethod mHttpMethod = HttpMethod.GET;
    IHttpManager httpManager = HttpManagerImpl.getInstance();
    private BaseTask task;
    private String paramsJson;
    private Map<String, String> paramsMap;
    private HandlerThread mHandlerThread = new HandlerThread("HttpRequest");
    private ResponseHandler mHandler = null;

    public HttpRequest(String url) {
        this.url = url;
        task = buildTask();
        mHandlerThread.start();
        mHandler = new ResponseHandler(this, mHandlerThread.getLooper());
    }

    public HttpRequest(String url, String params) {
        this.url = url;
        paramsJson = params;
        task = buildTask();
    }

    public HttpRequest setHead(HttpHead head) {
        mHead = head;
        return this;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public Map<String, String> getParamsMap() {
        return paramsMap;
    }

    public void setParamsMap(Map<String, String> paramsMap) {
        this.paramsMap = paramsMap;
    }

    public HttpHead getHead() {
        return mHead;
    }

    public String getUrl() {
        return url;
    }

    public HttpRequest setMethod(HttpMethod method) {
        mHttpMethod = method;
        return this;
    }

    public HttpMethod getHttpMethod() {
        return mHttpMethod;
    }

    public HttpRequest setResponseOnMainThread(boolean onMainThread) {
        isResponseOnMainThread = onMainThread;
        return this;
    }

    public HttpRequest setResponseListener(ResponseListener l) {
        this.listener = l;
        return this;
    }

    public BaseTask getTask() {
        return task;
    }

    public void submitRequest() {
        httpManager.submmitRequest(this);
    }

    public void onResponse(HttpResponse response) {
        HttpResponse tem = response;
        if (isResponseOnMainThread&&mHandler!=null) {
            mHandler.obtainMessage(HTTP_RESULT, tem).sendToTarget();
        } else {
            if (tem.getCode() == HttpCodes.HTTP_OK) {
                if (listener != null) {
                    listener.onSuccess(response.getResponse());
                }
            } else if (tem.getCode() == HttpCodes.HTTP_FAILURE) {
                listener.onFailure(response.getError());
            }
            mHandlerThread.quit();
        }
    }


    protected abstract BaseTask buildTask();

    public interface ResponseListener<T> {
        void onSuccess(T response);

        void onFailure(String error);
    }

    static class ResponseHandler extends Handler {
        WeakReference<HttpRequest> requestWeakReference;

        ResponseHandler(HttpRequest httpRequest, Looper looper) {
            super(looper);
            requestWeakReference = new WeakReference<>(httpRequest);
        }

        @Override
        public void handleMessage(Message msg) {
            HttpResponse response = (HttpResponse) msg.obj;
            if (response == null) {
                response = new HttpResponse(null, HttpCodes.HTTP_FAILURE, "server return null");
            }
            if (requestWeakReference == null || requestWeakReference.get() == null) {
                return;
            }
            HttpRequest httpRequest = requestWeakReference.get();
            if (response.getCode() == HttpCodes.HTTP_OK) {
                if (httpRequest.listener != null) {
                    httpRequest.listener.onSuccess(response.getResponse());
                }
            } else if (response.getCode() == HttpCodes.HTTP_FAILURE) {
                if (httpRequest.listener != null) {
                    httpRequest.listener.onFailure(response.getError());
                }
            }
            if (httpRequest.mHandlerThread != null)
                httpRequest.mHandlerThread.quit();
        }
    }

    public void shutDown() {
        httpManager.shutdownAll();
    }
}
