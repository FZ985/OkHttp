package fz.okhttplib.builder;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.file.download.DownLoadInfo2;
import fz.okhttplib.file.download.DownLoadListenerAdapter;
import fz.okhttplib.file.download.DownloadResponseBody2;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载监听
 * 多文件下载
 */
public class DownLoadBuilder3 {
    public static final ExecutorService THREAD_POOL = Executors
            .newFixedThreadPool(10);
    private final HashMap<Object, AsyncTask> downTasks = new HashMap<>();
    private final HashMap<Object, Integer> downState = new HashMap<>();
    private final int START = 1;
    private final int CANCEL = -1;

    public static DownLoadBuilder3 getInstance() {
        return DownLoadBuilderHolder.INSTANCE;
    }

    private static final class DownLoadBuilderHolder {
        private static volatile DownLoadBuilder3 INSTANCE = new DownLoadBuilder3();
    }

    public void download(final DownLoadInfo2 info, final @Nullable DownLoadListenerAdapter listener) {
        if (listener == null) return;
        if (info == null) return;
        if (downTasks.containsKey(info.url)) return;
        AsyncTask task = new DownloadTask(info, listener).executeOnExecutor(Executors.newCachedThreadPool(), info.url);
        downTasks.put(info.url, task);
        downState.put(info.url, START);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, Integer> {
        private DownLoadInfo2 info;
        private DownLoadListenerAdapter listener;

        private final int code = -1;
        private final int error0 = 0;
        private final int error1 = 1;
        private final int cancel = 5;
        private final int complete = 100;

        DownloadTask(DownLoadInfo2 info, DownLoadListenerAdapter listener) {
            this.info = info;
            this.listener = listener;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            long totalLen = getContentLength(info.url, listener);
            if (totalLen == 0) {
                return error1;
            }
            info.totalLength = totalLen;
            OkhttpUtil.log("download", "文件当前大小:" + info.currentLength + ",文件总大小:" + totalLen);
            if (info.currentLength == totalLen) {
                return complete;
            }
            Request req = new Request.Builder()
                    .url(info.url)
                    .header("RANGE", "bytes=" + info.currentLength + "-" + totalLen)//断点续传
                    .tag(info.url)
                    .build();
            try {
                Response response = OkHttpConfig.getInstance()
                        .client()
                        .newBuilder()
                        .addInterceptor(chain -> {
                            Response originalResponse = chain.proceed(chain.request());
                            listener.newResponse(originalResponse, info.file);
                            return originalResponse.newBuilder()
                                    .header("RANGE", "bytes=" + info.currentLength + "-" + totalLen)//断点续传
                                    .body(new DownloadResponseBody2(originalResponse, info, listener))
                                    .build();
                        })
                        .build()
                        .newCall(req)
                        .execute();
                if (response != null && response.isSuccessful()) {
                    long length = response.body().contentLength();
                    OkhttpUtil.log("download", "download:" + response + ",:" + length);
                    // 保存文件到本地
                    InputStream is = null;
                    RandomAccessFile randomAccessFile = null;
                    BufferedInputStream bis = null;

                    byte[] buff = new byte[2048];
                    int len;
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
                        return complete;
//                        listener.complete(info.file);
                    } catch (Exception e) {
                        OkhttpUtil.log("download", "down_exce1:" + e.getMessage());
                        if (downState.containsKey(info.url) && downState.get(info.url) == CANCEL)
                            return cancel;
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
                            OkhttpUtil.log("download", "down_exce:" + e.getMessage());
                        }
                    }
                } else
                    return error0;
            } catch (IOException e) {
                return error0;
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer codes) {
            super.onPostExecute(codes);
            OkhttpUtil.log("download", "响应码:" + codes);
            switch (codes) {
                case code:
                    break;
                case error0:
                    listener.error(new Exception("下载失败"));
                    break;
                case error1:
                    listener.error(new Exception("获取文件总大小失败"));
                    break;
                case complete:
                    listener.complete(info.file);
                    break;
                case cancel:
                    listener.cancel();
                    break;
            }
            downTasks.remove(info.url);
            downState.remove(info.url);
        }

        @Override
        protected void onCancelled(Integer integer) {
            super.onCancelled(integer);
            downState.remove(info.url);
            downTasks.remove(info.url);
            listener.cancel();
        }

        private long getContentLength(String downloadUrl, DownLoadListenerAdapter listener) {
            Request request = new Request.Builder().url(downloadUrl).build();
            try {
                Response response = OkHttpConfig.getInstance().client().newCall(request).execute();
                if (response != null && response.isSuccessful()) {
                    listener.newResponse(response, info.file);
                    long contentLength = response.body().contentLength();
                    response.body().close();
                    return contentLength;
                }
            } catch (IOException e) {
                OkhttpUtil.log("download", "获取文件长度请求失败:" + e.getMessage());
            }
            return 0L;
        }
    }

    private void clear(Object tag, boolean cancelAll) {
        for (Call call : OkHttpConfig.getInstance().client().dispatcher().queuedCalls()) {
            if (!cancelAll) {
                if (tag != null && tag.equals(call.request().tag())) {
                    call.cancel();
                }
            } else call.cancel();
        }
        for (Call call : OkHttpConfig.getInstance().client().dispatcher().runningCalls()) {
            if (!cancelAll) {
                if (tag != null && tag.equals(call.request().tag())) {
                    call.cancel();
                }
            } else call.cancel();
        }
        if (cancelAll) {
            for (Map.Entry entry : downTasks.entrySet()) {
                AsyncTask task = (AsyncTask) entry.getValue();
                task.cancel(true);
            }
            downTasks.clear();
        } else {
            if (downTasks.containsKey(tag)) {
                downState.put(tag, CANCEL);
                downTasks.get(tag).cancel(true);
                downTasks.remove(tag);
            }
        }
    }

    public void cancel(Object tag) {
        clear(tag, false);
    }

    public void cancelAll() {
        clear(null, true);
    }

}
