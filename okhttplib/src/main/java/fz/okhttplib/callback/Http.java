package fz.okhttplib.callback;

import android.os.Handler;

import java.io.File;

import fz.okhttplib.file.upload.UIProgressRequestListener;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public interface Http {

    int GET = 0;
    int POST = 1;
    int PUT = 2;
    int DELETE = 3;
    int HEAD = 4;
    int PATCH = 5;

    int NO = -1;
    int JSON = 1;
    int PARAMS = 2;
    int FORM = 3;

    //获取handler
    Handler obtainHandler();

    OkHttpClient client();

    OkHttpClient.Builder bulid();

    //取消指定请求
    void cancel(Object tag);

    //取消全部请求
    void cancelAll();

    CacheControl cacheControl();

    public interface Call {

        Response execute() throws Exception;

        Object executeObject() throws Exception;

        Response executeUploadFile(File[] fils, String[] fileKeys, UIProgressRequestListener uiProgressRequestListener) throws Exception;

        void enqueue(OkRequestCallback<?> callback);

        void enqueueUploadFile(File[] fils, String[] fileKeys, OkRequestCallback<?> callback, UIProgressRequestListener uiProgressRequestListener);

    }
}
