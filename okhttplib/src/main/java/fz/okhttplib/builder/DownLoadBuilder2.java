package fz.okhttplib.builder;

import android.os.AsyncTask;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.file.download.DownLoadInfo2;
import fz.okhttplib.file.download.DownLoadListenerAdapter;
import fz.okhttplib.file.download.DownloadResponseBody2;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载监听
 */
public class DownLoadBuilder2 {

    public static DownLoadBuilder2 getInstance() {
        return DownLoadBuilderHolder.INSTANCE;
    }

    private static final class DownLoadBuilderHolder {
        private static volatile DownLoadBuilder2 INSTANCE = new DownLoadBuilder2();
    }

    public void download(final DownLoadInfo2 info, final @Nullable DownLoadListenerAdapter listener) {
        if (listener == null) return;
        if (info == null) return;
        long totalLen = getContentLength(info.url, info.file, listener);
        if (totalLen == 0) {
            listener.error(new Exception("获取文件总大小失败"));
            return;
        }
        info.totalLength = totalLen;
        OkhttpUtil.log("download","文件当前大小:" + info.currentLength + ",文件总大小:" + totalLen);
        if (info.currentLength == totalLen) {
            listener.complete(info.file);
            return;
        }
        Request req = new Request.Builder()
                .url(info.url)
                .header("RANGE", "bytes=" + info.currentLength + "-" + totalLen)//断点续传
                .tag(info.url)
                .build();
        OkHttpConfig.getInstance()
                .client()
                .newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        listener.newResponse(originalResponse, info.file);
                        return originalResponse.newBuilder()
                                .header("RANGE", "bytes=" + info.currentLength + "-" + totalLen)//断点续传
                                .body(new DownloadResponseBody2(originalResponse, info, listener))
                                .build();
                    }
                })
                .build()
                .newCall(req)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        OkhttpUtil.log("download", "error:" + e.getMessage());
                        listener.error(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            long length = response.body().contentLength();
                            OkhttpUtil.log("download", response + ",:" + length);
                            // 保存文件到本地
                            InputStream is = null;
                            RandomAccessFile randomAccessFile = null;
                            BufferedInputStream bis = null;

                            byte[] buff = new byte[2048];
                            int len = 0;
                            try {
                                is = response.body().byteStream();
                                bis = new BufferedInputStream(is);

                                File file = info.tmpFile;
                                // 随机访问文件，可以指定断点续传的起始位置
                                randomAccessFile = new RandomAccessFile(file, "rwd");
                                randomAccessFile.seek(info.currentLength);
                                while ((len = bis.read(buff)) != -1) {
                                    randomAccessFile.write(buff, 0, len);
                                }

                                // 下载完成
                                info.tmpFile.renameTo(info.file);
                                listener.complete(info.file);
                            } catch (Exception e) {
                                OkhttpUtil.log("download","down_exce:" + e.getMessage());
                                listener.error(new Exception("下载失败"));
                            } finally {
                                try {
                                    if (is != null) {
                                        is.close();
                                    }
                                    if (bis != null) {
                                        bis.close();
                                    }
                                    if (randomAccessFile != null) {
                                        randomAccessFile.close();
                                    }
                                } catch (Exception e) {
                                    OkhttpUtil.log("download","down_exce:" + e.getMessage());
                                }
                            }
                        } else {
                            listener.error(new Exception("false," + response.code()));
                        }
                    }
                });
    }


    /**
     * 得到下载内容的完整大小
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl, File file, DownLoadListenerAdapter listener) {
        try {
            return new FileContentLengthTask(file, listener).executeOnExecutor(Executors.newCachedThreadPool(), downloadUrl).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        return 0;
    }

    public static class FileContentLengthTask extends AsyncTask<String, Void, Long> {
        private File file;
        private DownLoadListenerAdapter listener;

        public FileContentLengthTask(File file, DownLoadListenerAdapter listener) {
            this.file = file;
            this.listener = listener;
        }

        @Override
        protected Long doInBackground(String... urls) {
            String url = urls[0];
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = OkHttpConfig.getInstance().client().newCall(request).execute();
                if (response != null && response.isSuccessful()) {
                    listener.newResponse(response, file);
                    long contentLength = response.body().contentLength();
                    response.body().close();
                    return contentLength;
                }
            } catch (IOException e) {

            }
            return 0L;
        }
    }
}
