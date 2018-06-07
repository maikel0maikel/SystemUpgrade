package com.sinohb.system.upgrade.net.basic;


import com.sinohb.logger.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zWX396902 on 2018/3/30.
 */

public class HttpStringTask extends BaseTask {

    protected HttpStringTask(HttpRequest request) {
        super(request);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public HttpResponse call() throws Exception {

        Object object = super.call();
        if (object == null) {
            return new HttpResponse(null, HttpCodes.HTTP_FAILURE,"request is null");
        }
        BufferedReader bufferedReader = null;
        HttpResponse response = new HttpResponse();
        int code = 0;
        String result = null;
        if (object instanceof InputStream) {
            InputStream inputStream = null;
            try {
                inputStream = (InputStream) object;
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String readLine = null;
                StringBuilder builder = new StringBuilder();
                while ((readLine = bufferedReader.readLine()) != null) {
                    builder.append(readLine);
                }
                result = builder.toString();
                code = HttpCodes.HTTP_OK;
                builder.setLength(0);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                result = e.getMessage();
                code = HttpCodes.HTTP_FAILURE;
            } finally {
                IOUtils.closeQuietly(bufferedReader);
                IOUtils.closeQuietly(inputStream);
            }
            response.setResponse(result);
            response.setCode(code);
            return response;
        } else if (object instanceof HttpResponse) {
            return (HttpResponse) object;
        } else {
            return null;
        }
    }
}
