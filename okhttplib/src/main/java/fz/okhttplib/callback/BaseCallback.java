package fz.okhttplib.callback;

import android.text.TextUtils;

import com.google.gson.JsonParseException;

import java.io.IOException;

import androidx.lifecycle.Lifecycle;
import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.builder.MethodBuilder;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Create by JFZ
 * date: 2020-09-15 12:17
 **/
public abstract class BaseCallback implements Callback {
    private MethodBuilder methodBuilder;
    private Request request;
    private OkRequestCallback<?> callback;
    private long startReqTime;//开始请求时间

    public BaseCallback(MethodBuilder methodBuilder, Request request, OkRequestCallback<?> callback, long startReqTime) {
        this.methodBuilder = methodBuilder;
        this.request = request;
        this.callback = callback;
        this.startReqTime = startReqTime;
    }

    public void onFailure(final okhttp3.Call call, final IOException e) {
        final long responseTime = System.currentTimeMillis();
        log(request.url().toString() + "<<error响应时间start:" + startReqTime + ",end:" + responseTime + ",total:" + ((responseTime - startReqTime)) + "ms");
        OkHttpConfig.getInstance().obtainHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissLoad();
                if (call.isCanceled()) return;
                if (methodBuilder.getLifecycle() != null && methodBuilder.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                    return;
                }
                sendFailedCall(callback, -1, e);
            }
        });
    }

    @Override
    public void onResponse(final okhttp3.Call call, Response response) throws IOException {
        OkHttpConfig.getInstance().obtainHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissLoad();
                if (call.isCanceled()) return;
            }
        });
        if (response == null) {
            log("响应对象response 为空");
            return;
        }
        if (methodBuilder.getLifecycle() != null && methodBuilder.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        if (callback != null && callback.mType != null && callback.mType == Response.class) {
            sendSuccessCall(callback, response);
            return;
        }
        try {
            String str = response.body().string();
            String info = "";
            final int code = response.code();
            final boolean success = response.isSuccessful();
            final long responseTime = System.currentTimeMillis();
            log(request.url().toString() + "<<success响应时间start:" + startReqTime + ",end:" + responseTime + ",总计:" + (responseTime - startReqTime) + "ms");
            log(success + "," + code + "<<" + request.url().toString() + "<<返回数据:" + str);
            info = str;
            if (success) {
                if (callback != null && callback.mType != null && !TextUtils.isEmpty(info)) {
                    if (callback.mType == String.class) {
                        sendSuccessCall(callback, info);
                    } else {
                        Object o = OkhttpUtil.GSON.fromJson(info,
                                callback.mType);
                        if (o == null) {
                            sendFailedCall(callback, code, new Exception("Httppi:mGson.fromJson(finalStr,callback.mType) return null!"));
                        } else sendSuccessCall(callback, o);
                    }
                } else
                    sendFailedCall(callback, code, new Exception(":回调 or 返回数据 or 解析类型为空"));
            } else
                sendFailedCall(callback, code, new Exception(":response.isSuccessful() is not be true!"));
        } catch (JsonParseException e) {
            log("json解析失败:" + e.getMessage());
            sendFailedCall(callback, -1, e);
        } catch (Exception e) {
            log("请求失败：" + e.getMessage());
            sendFailedCall(callback, -1, e);
        }
    }

    protected abstract void dismissLoad();

    protected abstract void sendFailedCall(final OkRequestCallback callback, final int code, final Exception e);

    protected abstract void sendSuccessCall(final OkRequestCallback callback, final Object obj);

    private void log(String m) {
        OkhttpUtil.log("HttpApi", m);
    }
}
