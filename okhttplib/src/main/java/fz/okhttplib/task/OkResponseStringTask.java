package fz.okhttplib.task;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.Response;

/**
 * response.body().string() 必须在子线程中执行
 * 创建一个AsyncTask获取数据切换给主线程
 * okhttp 请求成功返回的response 获取body 数据
 */
public class OkResponseStringTask extends AsyncTask<Response, Void, String> {
    @Override
    protected String doInBackground(Response... responses) {
        Response response = responses[0];
        try {
            if (response != null) {
                return response.body().string();
            } else return "";
        } catch (IOException e) {
            return "";
        }
    }
}
