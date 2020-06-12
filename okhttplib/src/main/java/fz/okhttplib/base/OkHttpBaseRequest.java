package fz.okhttplib.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Create by JFZ
 * date: 2019-05-22 16:32
 * okhttp通用的请求基类，可传递请求头
 * <p>
 * headers:添加的请求头， 根据需求添加
 * params:请求参数，根据需求添加
 * cls: 请求成功返回的json对应的Javabean, 不添加返回String类型接送字符串（仅同步是用）
 * requestBean：请求参数，根据需求添加（仅post json时使用）
 * tag: 一个请求的标识
 **/
public class OkHttpBaseRequest {
    private List<OkHttpRequestHeaders> headers = null;//请求头
    //get、post请求时使用
    public HashMap<String, Object> requestParams;//请求参数集合
    public Class<?> cls;//请求成功时接收的bean对象
    //异步post请求时使用
    public Object requestBean;//请求参数对象
    public Object tag;//请求标识
    public long startReqTime;//开始请求时间

    public OkHttpBaseRequest() {
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(Object obj) {
        this.requestBean = obj;
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(HashMap<String, Object> requestParams) {
        this.requestParams = requestParams;
        this.requestBean = requestParams;
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(Object obj, List<OkHttpRequestHeaders> headers) {
        this.requestBean = obj;
        this.headers = headers;
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(Class<?> cls, Object requestBean) {
        this.cls = cls;
        this.requestBean = requestBean;
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(Class<?> cls, HashMap<String, Object> requestParams) {
        this.cls = cls;
        this.requestParams = requestParams;
        this.requestBean = requestParams;
        startReqTime = System.currentTimeMillis();
    }

    public OkHttpBaseRequest(Class<?> cls) {
        this.cls = cls;
        startReqTime = System.currentTimeMillis();
    }


    public void addHeader(String key, String value) {
        if (headers == null) headers = new ArrayList<>();
        headers.add(new OkHttpRequestHeaders(key, value));
    }

    public List<OkHttpRequestHeaders> getHeaders() {
        if (headers == null) headers = new ArrayList<>();
        return headers;
    }
}
