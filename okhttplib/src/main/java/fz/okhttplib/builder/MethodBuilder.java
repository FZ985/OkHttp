package fz.okhttplib.builder;


import android.app.Activity;
import android.text.TextUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import fz.okhttplib.base.OkHttpBaseRequest;
import fz.okhttplib.callback.Http;
import fz.okhttplib.callback.Loadding;
import fz.okhttplib.callback.OkRequestCallback;
import fz.okhttplib.file.upload.UIProgressRequestListener;
import okhttp3.Response;

public class MethodBuilder implements Http.Call {

    private String url;
    private OkHttpBaseRequest request;
    private Loadding loadding;
    private int type = -1, rType = -1;
    private Lifecycle mLifecycleRegistry;
    private RequestCall call;

    @Deprecated
    public MethodBuilder(int type, int rType) {
        this.type = type;
        this.rType = rType;
        this.call = new RequestCall(this);
    }

    public MethodBuilder(String url, int type, int rType) {
        this.type = type;
        this.rType = rType;
        this.url = url;
        this.call = new RequestCall(this);
    }

    /**
     * 过时，但仍可以使用
     */
    @Deprecated
    public MethodBuilder url(String url) {
        this.url = url;
        return this;
    }

    public String url() {
        return this.url;
    }

    public MethodBuilder request(OkHttpBaseRequest request) {
        this.request = request;
        return this;
    }

    public OkHttpBaseRequest request() {
        return this.request;
    }

    public MethodBuilder load(Loadding loadding) {
        this.loadding = loadding;
        return this;
    }

    public Loadding load() {
        return this.loadding;
    }

    public MethodBuilder bind(Activity activity) {
        if (activity != null && activity instanceof AppCompatActivity) {
            return bind(((AppCompatActivity) new WeakReference<>(activity).get()).getLifecycle());
        }
        return this;
    }

    public MethodBuilder bind(Fragment fragment) {
        if (fragment != null) {
            return bind(fragment.getLifecycle());
        }
        return this;
    }

    public MethodBuilder bind(Lifecycle lifecycle) {
        this.mLifecycleRegistry = lifecycle;
        return this;
    }

    @NonNull
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    public int type() {
        return this.type;
    }

    public int rType() {
        return this.rType;
    }

    @Deprecated
    public Http.Call build() {
        return call;
    }

    @Override
    public Response execute() throws Exception {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url is null!");
        }
        return call.execute();
    }

    @Override
    public Object executeObject() throws Exception {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url is null!");
        }
        return call.executeObject();
    }

    @Override
    public Response executeUploadFile(File[] fils, String[] fileKeys, UIProgressRequestListener uiProgressRequestListener) throws Exception {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url is null!");
        }
        if (fils == null || fils.length == 0 || fileKeys == null || fileKeys.length == 0) {
            throw new IllegalArgumentException("Please check files or filekeys!");
        }
        return call.executeUploadFile(fils, fileKeys, uiProgressRequestListener);
    }

    @Override
    public void enqueue(OkRequestCallback<?> callback) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url is null!");
        }
        call.enqueue(callback);
    }

    @Override
    public void enqueueUploadFile(File[] fils, String[] fileKeys, OkRequestCallback<?> callback, UIProgressRequestListener uiProgressRequestListener) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url is null!");
        }
        if (fils == null || fils.length == 0 || fileKeys == null || fileKeys.length == 0) {
            throw new IllegalArgumentException("Please check files or filekeys!");
        }
        call.enqueueUploadFile(fils, fileKeys, callback, uiProgressRequestListener);
    }
}
