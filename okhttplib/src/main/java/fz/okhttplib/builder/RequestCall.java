package fz.okhttplib.builder;

import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.Executors;

import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.callback.BaseCallback;
import fz.okhttplib.callback.Http;
import fz.okhttplib.callback.OkRequestCallback;
import fz.okhttplib.file.upload.UIProgressRequestListener;
import fz.okhttplib.task.OkHttpAsyncTask;
import fz.okhttplib.task.OkResponseStringTask;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Request;
import okhttp3.Response;

public class RequestCall implements Http.Call {
    private OkHttpAsyncTask task;
    private MethodBuilder methodBuilder;
    private long startReqTime;//开始请求时间

    RequestCall(MethodBuilder builder) {
        this.methodBuilder = builder;
        startReqTime = System.currentTimeMillis();
    }

    @Override
    public Response execute() throws Exception {
        return executeUploadFile(null, null, null);
    }

    @Override
    public Object executeObject() throws Exception {
        try {
            Response response = execute();
            if (response == null) return null;
            String finalObj = new OkResponseStringTask().executeOnExecutor(Executors.newCachedThreadPool(), response).get();
            boolean success = response.isSuccessful();
            int code = response.code();
            log("##success:" + success + ",code:" + code + ",解析前数据:" + finalObj);
            if (success && !TextUtils.isEmpty(finalObj)) {
                if (methodBuilder.request() != null && methodBuilder.request().cls != null && methodBuilder.request().cls != String.class) {
                    return OkhttpUtil.GSON.fromJson(finalObj, methodBuilder.request().cls);
                } else
                    return finalObj;
            }
        } catch (Exception e) {
            log("###" + OkhttpUtil.typeTag(methodBuilder) + "同步请求出错:" + e.getMessage());
        }
        return null;
    }

    @Override
    public Response executeUploadFile(File[] fils, String[] fileKeys, UIProgressRequestListener uiProgressRequestListener) throws Exception {
        if (TextUtils.isEmpty(methodBuilder.url())) {
            throw new NullPointerException("url can not be null!");
        }
        if (!(methodBuilder.url().startsWith("http") || methodBuilder.url().startsWith("https"))) {
            throw new IllegalArgumentException("The url prefix is not http or https!");
        }
        showLoad();
        return getTask(OkhttpUtil.getRequest(methodBuilder, fils, fileKeys, uiProgressRequestListener));
    }

    @Override
    public void enqueue(OkRequestCallback<?> callback) {
        enqueueUploadFile(null, null, callback, null);
    }

    @Override
    public void enqueueUploadFile(File[] filss, String[] fileKeys, OkRequestCallback<?> callback, UIProgressRequestListener uiProgressRequestListener) {
        if (TextUtils.isEmpty(methodBuilder.url())) {
            sendFailedCall(callback, -1, new NullPointerException("url can not be null!"));
            return;
        }
        if (!(methodBuilder.url().startsWith("http") || methodBuilder.url().startsWith("https"))) {
            sendFailedCall(callback, -1, new IllegalArgumentException("The url prefix is not http or https!"));
            return;
        }
        Request request = OkhttpUtil.getRequest(methodBuilder, filss, fileKeys, uiProgressRequestListener);
        if (request == null) return;
        showLoad();
        if (methodBuilder.request() != null) {
            startReqTime = methodBuilder.request().startReqTime;
        }
        log(methodBuilder.url() + "<<开始请求时间:" + startReqTime);
        asyncCall(request, callback);
    }

    private void asyncCall(final Request request, final OkRequestCallback<?> callback) {
        OkHttpConfig.getInstance().client().newCall(request).enqueue(new BaseCallback(methodBuilder, request, callback, startReqTime) {
            @Override
            protected void dismissLoad() {
                RequestCall.this.dismissLoad();
            }

            @Override
            protected void sendFailedCall(OkRequestCallback callback, int code, Exception e) {
                RequestCall.this.sendFailedCall(callback, code, e);
            }

            @Override
            protected void sendSuccessCall(OkRequestCallback callback, Object obj) {
                RequestCall.this.sendSuccessCall(callback, obj);
            }
        });
    }

    private Response getTask(Request req) throws Exception {
        if (task != null) task.cancel(true);
        task = null;
        task = new OkHttpAsyncTask(methodBuilder.load());
        task.executeOnExecutor(Executors.newCachedThreadPool(), req);
        Response response = task.get();
        if (response != null) {
            task.cancel(true);
            task = null;
        }
        return response;
    }

    private void sendFailedCall(final OkRequestCallback callback, final int code, final Exception e) {
        OkHttpConfig.getInstance().obtainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) callback.onError(code, e);
            }
        });
    }

    private void sendSuccessCall(final OkRequestCallback callback, final Object obj) {
        OkHttpConfig.getInstance().obtainHandler().post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) callback.onResponse(obj);
            }
        });
    }

    private void showLoad() {
        if (methodBuilder.load() != null && !methodBuilder.load().isShowing())
            methodBuilder.load().show();
    }

    private void dismissLoad() {
        if (methodBuilder.load() != null && methodBuilder.load().isShowing())
            methodBuilder.load().dismiss();
    }

    private void log(String m) {
        OkhttpUtil.log("HttpApi", m);
    }
}
