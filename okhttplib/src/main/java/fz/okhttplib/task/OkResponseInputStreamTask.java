package fz.okhttplib.task;

import android.os.AsyncTask;

import java.io.InputStream;

import okhttp3.Response;

/**
 * response.body().byteStream()
 * 子线程中执行
 * 创建一个AsyncTask获取数据切换给主线程
 * okhttp 请求成功返回的response 获取body 数据
 */
public class OkResponseInputStreamTask extends AsyncTask<Response, Void, InputStream> {
    @Override
    protected InputStream doInBackground(Response... responses) {
        Response response = responses[0];
            return response.body().byteStream();
    }
}
