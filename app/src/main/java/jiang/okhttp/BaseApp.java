package jiang.okhttp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.tool.ResponeInterceptor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Create by JFZ
 * date: 2020-09-02 12:00
 **/
public class BaseApp extends Application {

    private static BaseApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        OkHttpConfig.getInstance()
                .addInterceptor(new ResponeInterceptor() {
                    @Override
                    public void onIntercept(String url, String result) {
                        System.out.println("拦截响应url:" + url);
                        Log.e("拦截响应", "result:" + result);
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder build = chain.request().newBuilder();
                        Request request = build.build();
                        if (!isConnection(BaseApp.getInstance())) {
                            OkHttpConfig.getInstance().obtainHandler().post(() -> {
                                Toast.makeText(BaseApp.this, "没网", Toast.LENGTH_SHORT).show();
                            });
                        }
                        return chain.proceed(request);
                    }
                }).newBuild();
    }

    public static BaseApp getInstance() {
        return app;
    }

    public static boolean isConnection(Context context) {
        ConnectivityManager manager = getConnectivityManager(context);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable;
        if (networkInfo != null) {
            isAvailable = networkInfo.isAvailable();
        } else {
            isAvailable = false;
        }
        Log.i("ConnectionVerdict", isAvailable + "");
        return isAvailable;
    }

    /**
     * 获取联网的Manager
     *
     * @param context
     * @return
     */
    private static ConnectivityManager getConnectivityManager(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnectivityManager == null) {
            return null;
        }
        return mConnectivityManager;
    }
}
