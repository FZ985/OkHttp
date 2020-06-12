package fz.okhttplib.builder;

import android.text.TextUtils;

import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import androidx.lifecycle.Lifecycle;
import fz.okhttplib.base.OkHttpBaseRequest;
import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.base.OkHttpRequestHeaders;
import fz.okhttplib.base.Params;
import fz.okhttplib.callback.Http;
import fz.okhttplib.callback.OkRequestCallback;
import fz.okhttplib.file.upload.UIProgressRequestListener;
import fz.okhttplib.task.OkHttpAsyncTask;
import fz.okhttplib.task.OkResponseStringTask;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestCall implements Http.Call {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
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
            log("###" + typeTag() + "同步请求出错:" + e.getMessage());
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
        return getTask(getRequest(fils, fileKeys, uiProgressRequestListener));
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
        Request request = getRequest(filss, fileKeys, uiProgressRequestListener);
        if (request == null) return;
        showLoad();
        if (methodBuilder.request() != null) {
            startReqTime = methodBuilder.request().startReqTime;
        }
        log(methodBuilder.url() + "<<开始请求时间:" + startReqTime);
        asyncCall(request, callback);
    }

    private void asyncCall(final Request request, final OkRequestCallback<?> callback) {
        OkHttpConfig.getInstance().client().newCall(request).enqueue(new Callback() {
            @Override
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
        });
    }

    private Request.Builder getBuilder(OkHttpBaseRequest request, RequestBody body) {
        Request.Builder builder = new Request.Builder();
        if (request != null && request.getHeaders().size() > 0) {
            for (OkHttpRequestHeaders header : request.getHeaders()) {
                log("添加请求头key:" + header.key + ",value:" + header.value);
                builder.addHeader(header.key, header.value);
            }
        }
        switch (methodBuilder.type()) {
            default:
            case Http.GET:
                builder.get();
                break;
            case Http.POST:
                builder.post(body);
                break;
            case Http.PUT:
                builder.put(body);
                break;
            case Http.DELETE:
                if (body == null) builder.delete();
                else builder.delete(body);
                break;
            case Http.HEAD:
                builder.head();
                break;
            case Http.PATCH:
                builder.patch(body);
                break;
        }
        return builder;
    }

    private Request getRequest(File[] fils, String[] fileKeys, UIProgressRequestListener uiProgressRequestListener) {
        switch (methodBuilder.type()) {
            default:
            case Http.GET:
                String finalUrl = methodBuilder.url();
                if (methodBuilder.request() != null) {
                    finalUrl = methodBuilder.url() + OkhttpUtil.appendParams(OkhttpUtil.validateParam(OkhttpUtil.map2Params(methodBuilder.request().requestParams)));
                }
                log("#####" + typeTag() + "请求:" + finalUrl);
                return getBuilder(methodBuilder.request(), null).url(finalUrl).tag(methodBuilder.request() == null ? methodBuilder.url() : methodBuilder.request().tag).cacheControl(OkHttpConfig.getInstance().cacheControl()).build();
            case Http.POST:
            case Http.PUT:
            case Http.DELETE:
            case Http.HEAD:
            case Http.PATCH:
                if (methodBuilder.rType() == Http.NO) {
                    return getBuilder(methodBuilder.request(), null).url(methodBuilder.url()).tag(methodBuilder.url()).build();
                } else if (methodBuilder.rType() == Http.JSON) {
                    String json = (methodBuilder.request() != null && methodBuilder.request().requestBean != null) ? OkhttpUtil.reqParams(methodBuilder.request().requestBean) : "";
                    log("#####" + typeTag() + "请求:" + methodBuilder.url() + "<<请求json:" + json);
                    return getBuilder(methodBuilder.request(), RequestBody.create(JSON, json)).url(methodBuilder.url()).tag(methodBuilder.request() == null ? methodBuilder.url() : methodBuilder.request().tag).build();
                } else if (methodBuilder.rType() == Http.PARAMS) {
                    FormBody.Builder builder = new FormBody.Builder();
                    StringBuilder str = new StringBuilder();
                    if (methodBuilder.request() != null) {//key value传值请求
                        for (Params param : OkhttpUtil.validateParam(OkhttpUtil.map2Params(methodBuilder.request().requestParams))) {
                            builder.add(param.key, param.value);
                            str.append(param.key + ":" + param.value + ";");
                        }
                    }
                    log("#####" + typeTag() + "请求:" + methodBuilder.url() + "<<请求参数>>:" + str.toString());
                    return getBuilder(methodBuilder.request(), builder.build()).url(methodBuilder.url()).tag(methodBuilder.request() == null ? methodBuilder.url() : methodBuilder.request().tag).build();
                } else if (methodBuilder.rType() == Http.FORM) {
                    return buildMultipartFormRequest(fils, fileKeys, uiProgressRequestListener);
                }
        }
        return null;
    }

    private Request buildMultipartFormRequest(File[] files, String[] fileKeys, UIProgressRequestListener uiProgressRequestListener) {
        Params[] params = (methodBuilder.request() != null ? OkhttpUtil.validateParam(OkhttpUtil.map2Params(methodBuilder.request().requestParams)) : OkhttpUtil.validateParam(null));
        MultipartBody.Builder builder = new MultipartBody.Builder("AaB03x");
        builder.setType(MultipartBody.FORM);
        for (Params param : params) {
            log("##请求数据##key:" + param.key + ",value:" + param.value);
            builder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\""
                            + param.key + "\""),
                    RequestBody.create(null, param.value));
        }
        if (files != null && fileKeys != null && files.length == fileKeys.length) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(
                        MediaType.parse(OkhttpUtil.guessMimeType(fileName)), file);
                // TODO 根据文件名设置contentType
                builder.addPart(
                        Headers.of("Content-Disposition", "form-data; name=\""
                                + fileKeys[i] + "\"; filename=\"" + fileName
                                + "\""), fileBody);
            }
        }
        RequestBody requestBody = builder.build();
        return getBuilder(methodBuilder.request(), uiProgressRequestListener == null ? requestBody : OkHttpConfig.ProgressHelper.addProgressRequestListener(requestBody, uiProgressRequestListener)).url(methodBuilder.url()).tag(methodBuilder.url()).build();
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
                if (callback != null) callback.onError(code,e);
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

    private String typeTag() {
        if (methodBuilder.type() == Http.GET) return "get";
        else if (methodBuilder.type() == Http.POST) return "post";
        else if (methodBuilder.type() == Http.PUT) return "put";
        else if (methodBuilder.type() == Http.DELETE) return "delete";
        else if (methodBuilder.type() == Http.HEAD) return "head";
        else if (methodBuilder.type() == Http.PATCH) return "patch";
        else return "";
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
