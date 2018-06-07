package com.sinohb.system.upgrade.net.basic;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by maikel on 2018/3/31.
 */

public class BaseTask implements Callable {
    private static final String TAG = "BaseTask";
    private static final int READ_TIME_OUT = 1000 * 10;
    private static final int CONNECT_TIME_OUT = READ_TIME_OUT;
    protected HttpRequest mRequest;

    protected BaseTask(HttpRequest request) {
        this.mRequest = request;
    }

    /**
     * 设置不验证主机
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        }};
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call() throws Exception {
        if (this.mRequest == null) {
            return new HttpResponse(null, HttpCodes.HTTP_FAILURE, "request is null");
        }
        if (this.mRequest.getUrl() == null || this.mRequest.getUrl().length() == 0) {

            return new HttpResponse(null, HttpCodes.HTTP_FAILURE, "your url is empty please check");
        }
        HttpURLConnection con = null;
        HttpResponse response = new HttpResponse();
        int code = 0;
        String result = null;
        try {
            String requestUrl = mRequest.getUrl();
            byte[] postData = null;
            if (mRequest.getParamsMap() != null) {
                StringBuilder tempParams = new StringBuilder();
                int pos = 0;
                for (String key : mRequest.getParamsMap().keySet()) {
                    if (pos > 0) {
                        tempParams.append("&");
                    }
                    tempParams.append(String.format("%s=%s", key, URLEncoder.encode(mRequest.getParamsMap().get(key), "utf-8")));
                    pos++;
                }
                if (mRequest.getHttpMethod() == HttpMethod.GET)
                    requestUrl += tempParams.toString();
                else
                    postData = tempParams.toString().getBytes("utf-8");
                tempParams.setLength(0);
            }
            if (mRequest.getParamsJson() != null && mRequest.getParamsJson().length() > 0) {
                if (mRequest.getHttpMethod() == HttpMethod.GET)
                    requestUrl += mRequest.getParamsJson();
                else
                    postData = mRequest.getParamsJson().getBytes("utf-8");
            }
            URL url = new URL(requestUrl);
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                con = https;
            } else {
                con = (HttpURLConnection) url.openConnection();
            }
            con.setRequestProperty("Content-Type", "application/json");

            if (mRequest.getHead() != null) {
                con.setRequestProperty(mRequest.getHead().getKey(), mRequest.getHead().getValue());
            }
            con.setConnectTimeout(CONNECT_TIME_OUT);
            con.setReadTimeout(READ_TIME_OUT);
            if (mRequest.getHttpMethod() == HttpMethod.GET) {
                con.setRequestMethod("GET");
            } else if (mRequest.getHttpMethod() == HttpMethod.POST) {
                con.setRequestMethod("POST");
                if (postData != null) {
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setUseCaches(false);
                    con.setInstanceFollowRedirects(true);
                    con.connect();
                    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                    dos.write(postData);
                    dos.flush();
                    dos.close();
                }
            }
            InputStream is = con.getInputStream();
            return is;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            result = e.getMessage();
            code = HttpCodes.HTTP_FAILURE;
        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
            code = HttpCodes.HTTP_FAILURE;
        }
        response.setResponse(result);
        response.setCode(code);
        return response;
    }
}
