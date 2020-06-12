package fz.okhttplib;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.builder.DownLoadBuilder;
import fz.okhttplib.builder.MethodBuilder;
import fz.okhttplib.callback.Http;
import fz.okhttplib.file.download.DownLoadInfo;
import fz.okhttplib.file.download.DownLoadListenerAdapter;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class HttpImpl {

    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(20);

    private static List<Interceptor> interceptorss = new ArrayList<>();

    public static MethodBuilder get(String url) {
        return new MethodBuilder(url, Http.GET, Http.NO);
    }

    public static MethodBuilder postJson(String url) {
        return new MethodBuilder(url, Http.POST, Http.JSON);
    }

    public static MethodBuilder postParams(String url) {
        return new MethodBuilder(url, Http.POST, Http.PARAMS);
    }

    public static MethodBuilder postForm(String url) {
        return new MethodBuilder(url, Http.POST, Http.FORM);
    }

    public static MethodBuilder putJson(String url) {
        return new MethodBuilder(url, Http.PUT, Http.JSON);
    }

    public static MethodBuilder putParams(String url) {
        return new MethodBuilder(Http.PUT, Http.PARAMS);
    }

    public static MethodBuilder putForm(String url) {
        return new MethodBuilder(url, Http.PUT, Http.FORM);
    }

    public static MethodBuilder delete(String url) {
        return new MethodBuilder(url, Http.DELETE, Http.NO);
    }

    public static MethodBuilder deleteJson(String url) {
        return new MethodBuilder(url, Http.DELETE, Http.JSON);
    }

    public static MethodBuilder deleteParams(String url) {
        return new MethodBuilder(url, Http.DELETE, Http.PARAMS);
    }

    public static MethodBuilder deleteForm(String url) {
        return new MethodBuilder(url, Http.DELETE, Http.FORM);
    }

    public static MethodBuilder headParams(String url) {
        return new MethodBuilder(url, Http.HEAD, Http.PARAMS);
    }

    public static MethodBuilder headForm(String url) {
        return new MethodBuilder(url, Http.HEAD, Http.FORM);
    }

    public static MethodBuilder patchJson(String url) {
        return new MethodBuilder(url, Http.PATCH, Http.JSON);
    }

    public static MethodBuilder patchParams(String url) {
        return new MethodBuilder(url, Http.PATCH, Http.PARAMS);
    }

    public static MethodBuilder patchForm(String url) {
        return new MethodBuilder(url, Http.PATCH, Http.FORM);
    }

    public static void download(String url, String path, String fileName, DownLoadListenerAdapter call) {
        DownLoadBuilder.getInstance().download(new DownLoadInfo(url, path, fileName), call);
    }

    public static void enable(boolean logDebug, boolean isProxy) {
        OkHttpConfig.isProxy = isProxy;
        OkhttpUtil.LogDebug(logDebug);
    }

    public static void cookie(CookieJar cookieJar) {
        OkHttpConfig.cookieJar = cookieJar;
    }

    public static void clientBuilder(OkHttpClient.Builder OKHTTPCLIENT_BUILDER) {
        OkHttpConfig.OKHTTPCLIENT_BUILDER = OKHTTPCLIENT_BUILDER;
    }

    public static void logInterceptor(boolean logInterceptor) {
        OkHttpConfig.logInterceptor = logInterceptor;
    }

    public static void addInterceptors(List<Interceptor> interceptors) {
        if (interceptorss == null) interceptorss = new ArrayList<>();
        if (interceptors != null) {
            interceptorss.addAll(interceptors);
        }
        OkHttpConfig.interceptors = interceptorss;
    }

    public static void addInterceptor(Interceptor interceptor) {
        if (interceptorss == null) interceptorss = new ArrayList<>();
        if (interceptor != null) interceptorss.add(interceptor);
        OkHttpConfig.interceptors = interceptorss;
    }

    /***==过时函数，但仍可以使用=====================================================***/
    @Deprecated
    public static MethodBuilder get() {
        return new MethodBuilder(Http.GET, Http.NO);
    }

    @Deprecated
    public static MethodBuilder postJson() {
        return new MethodBuilder(Http.POST, Http.JSON);
    }

    @Deprecated
    public static MethodBuilder postParams() {
        return new MethodBuilder(Http.POST, Http.PARAMS);
    }

    @Deprecated
    public static MethodBuilder postForm() {
        return new MethodBuilder(Http.POST, Http.FORM);
    }

    @Deprecated
    public static MethodBuilder putJson() {
        return new MethodBuilder(Http.PUT, Http.JSON);
    }

    @Deprecated
    public static MethodBuilder putParams() {
        return new MethodBuilder(Http.PUT, Http.PARAMS);
    }

    @Deprecated
    public static MethodBuilder putForm() {
        return new MethodBuilder(Http.PUT, Http.FORM);
    }

    @Deprecated
    public static MethodBuilder delete() {
        return new MethodBuilder(Http.DELETE, Http.NO);
    }

    @Deprecated
    public static MethodBuilder deleteJson() {
        return new MethodBuilder(Http.DELETE, Http.JSON);
    }

    @Deprecated
    public static MethodBuilder deleteParams() {
        return new MethodBuilder(Http.DELETE, Http.PARAMS);
    }

    @Deprecated
    public static MethodBuilder headJson() {
        return new MethodBuilder(Http.HEAD, Http.JSON);
    }

    @Deprecated
    public static MethodBuilder deleteForm() {
        return new MethodBuilder(Http.DELETE, Http.FORM);
    }

    @Deprecated
    public static MethodBuilder headParams() {
        return new MethodBuilder(Http.HEAD, Http.PARAMS);
    }

    @Deprecated
    public static MethodBuilder headForm() {
        return new MethodBuilder(Http.HEAD, Http.FORM);
    }

    @Deprecated
    public static MethodBuilder patchJson() {
        return new MethodBuilder(Http.PATCH, Http.JSON);
    }

    @Deprecated
    public static MethodBuilder patchParams() {
        return new MethodBuilder(Http.PATCH, Http.PARAMS);
    }

    @Deprecated
    public static MethodBuilder patchForm() {
        return new MethodBuilder(Http.PATCH, Http.FORM);
    }
}
