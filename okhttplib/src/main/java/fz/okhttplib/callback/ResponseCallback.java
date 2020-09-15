package fz.okhttplib.callback;

import okhttp3.Response;

/**
 * Create by JFZ
 * date: 2020-09-15 12:13
 **/
public interface ResponseCallback {

    void onResponseStart(ResponseCallback callback, Response response);

    void onResponse(Response response);
}
