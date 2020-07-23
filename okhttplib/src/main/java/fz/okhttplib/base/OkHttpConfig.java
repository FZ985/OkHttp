package fz.okhttplib.base;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import fz.okhttplib.callback.Http;
import fz.okhttplib.callback.ProgressRequestListener;
import fz.okhttplib.file.upload.ProgressRequestBody;
import fz.okhttplib.tool.OkHttpLoggerInterceptor;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpConfig implements Http {
    private boolean isProxy = true;
    private boolean logInterceptor = false;
    private static OkHttpConfig INSTANCE;
    private OkHttpClient mOkHttpClient;
    private OkHttpClient.Builder mBuilder;
    private Handler mDelivery;
    private CookieJar cookieJar;
    private OkHttpClient.Builder OKHTTPCLIENT_BUILDER;
    private List<Interceptor> interceptors;
    private List<Interceptor> netInterceptors;
    private static final long cacheSize = 50 * 1024 * 1024;//缓存大小为50M
    private String cachePath;
    private CacheControl cacheControl;

    @Override
    public Handler obtainHandler() {
        return mDelivery;
    }

    @Override
    public OkHttpClient client() {
        return mOkHttpClient;
    }

    @Override
    public OkHttpClient.Builder bulid() {
        return mBuilder;
    }

    @Override
    public void cancel(Object tag) {
        cancel(false, tag);
    }

    @Override
    public void cancelAll() {
        cancel(true, null);
    }

    private void cancel(boolean isCancelAll, Object tag) {
        if (mOkHttpClient == null) return;
        for (okhttp3.Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (!isCancelAll) {
                if (tag != null && tag.equals(call.request().tag())) {
                    call.cancel();
                }
            } else call.cancel();
        }
        for (okhttp3.Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (!isCancelAll) {
                if (tag != null && tag.equals(call.request().tag())) {
                    call.cancel();
                }
            } else call.cancel();
        }
    }

    public static OkHttpConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (OkHttpConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OkHttpConfig();
                }
            }
        }
        return INSTANCE;
    }

    private OkHttpConfig() {
        Log.e("OkHttpConf", " OkHttpConf init..");
        newBuild();
        mDelivery = new Handler(Looper.getMainLooper());
        //设置缓存时间为10小时
        cacheControl = new CacheControl.Builder()
                .maxAge(10, TimeUnit.HOURS)
                .noStore()
                .build();
    }

    @SuppressLint("TrulyRandom")
    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new OkTrustAllCerts()},
                    new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    public void newBuild() {
        if (mOkHttpClient != null) {
            mBuilder = (OKHTTPCLIENT_BUILDER != null) ? OKHTTPCLIENT_BUILDER : mOkHttpClient.newBuilder();
            init();
        } else {
            if (OKHTTPCLIENT_BUILDER == null) {
                mBuilder = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .sslSocketFactory(createSSLSocketFactory())// 信任所有证书~
//                .sslSocketFactory(createSSLSocketFactory(), new OkTrustAllCerts()) // 信任所有证书~
                        .hostnameVerifier(new OkTrustAllHostnameVerifier());
                init();
            } else {
                mBuilder = OKHTTPCLIENT_BUILDER;
            }
        }
        mOkHttpClient = mBuilder.build();
    }

    public OkHttpConfig isProxy(boolean isProxy) {
        this.isProxy = isProxy;
        return this;
    }

    public OkHttpConfig cachePath(String cachePath) {
        this.cachePath = cachePath;
        return this;
    }

    public OkHttpConfig cookie(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }

    public OkHttpConfig logInterceptor(boolean logInterceptor) {
        this.logInterceptor = logInterceptor;
        return this;
    }

    public OkHttpConfig addInterceptor(Interceptor interceptor) {
        if (this.interceptors == null) this.interceptors = new ArrayList<>();
        if (interceptor != null) this.interceptors.add(interceptor);
        return this;
    }

    public OkHttpConfig addNetInterceptor(Interceptor interceptor) {
        if (this.netInterceptors == null) this.netInterceptors = new ArrayList<>();
        if (interceptor != null) this.netInterceptors.add(interceptor);
        return this;
    }

    public OkHttpConfig clientBuilder(OkHttpClient.Builder OKHTTPCLIENT_BUILDER) {
        this.OKHTTPCLIENT_BUILDER = OKHTTPCLIENT_BUILDER;
        return this;
    }

    private void init() {
        if (!TextUtils.isEmpty(cachePath)) {
            mBuilder.cache(new Cache(new File(cachePath), cacheSize));
        }
        if (isProxy) {
            mBuilder.proxy(Proxy.NO_PROXY);
        }
        if (cookieJar != null) {
            mBuilder.cookieJar(cookieJar);
        }
        if (logInterceptor) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new OkHttpLoggerInterceptor());
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            mBuilder.addInterceptor(interceptor);
        }
        if (interceptors != null && interceptors.size() > 0) {
            for (Interceptor interceptor : interceptors) {
                mBuilder.addInterceptor(interceptor);
            }
        }
        if (netInterceptors != null && netInterceptors.size() > 0) {
            for (Interceptor interceptor : netInterceptors) {
                mBuilder.addNetworkInterceptor(interceptor);
            }
        }
    }

    @Override
    public CacheControl cacheControl() {
        return cacheControl;
    }

    public class OkTrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public class OkTrustAllHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    public static class ProgressHelper {

        /**
         * 包装请求体用于上传文件的回调
         *
         * @param requestBody             请求体RequestBody
         * @param progressRequestListener 进度回调接口
         * @return 包装后的进度回调请求体
         */
        public static ProgressRequestBody addProgressRequestListener(RequestBody requestBody, ProgressRequestListener progressRequestListener) {
            //包装请求体
            return new ProgressRequestBody(requestBody, progressRequestListener);
        }
    }
}
