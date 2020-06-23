package fz.okhttplib.builder;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import fz.okhttplib.base.OkHttpConfig;
import fz.okhttplib.file.download.DownLoadInfo;
import fz.okhttplib.file.download.DownLoadListenerAdapter;
import fz.okhttplib.file.download.DownloadResponseBody;
import fz.okhttplib.tool.OkhttpUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DownLoadBuilder {
    private boolean isRunning = false;
    private long compeleteSize;

    public static DownLoadBuilder getInstance() {
        return DownLoadBuilderHolder.INSTANCE;
    }

    private static final class DownLoadBuilderHolder {
        private static volatile DownLoadBuilder INSTANCE = new DownLoadBuilder();
    }

    public void download(final DownLoadInfo info, final DownLoadListenerAdapter listener) {
        if (isRunning) return;
        if (listener == null) return;
        if (info != null) {
            File dir = new File(info.path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 当前下载的进度
            final File file = new File(dir, info.fileName);// 获取下载文件
            // 当前下载的进度
            compeleteSize = 0;
            if (!file.exists()) {
                // 如果文件不存在
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                file.delete();
            }
            info.file = file;
            Request req = new Request.Builder()
                    .url(info.url)
                    .header("RANGE", "bytes=" + compeleteSize + "-")//断点续传
                    .build();
            OkHttpConfig.getInstance()
                    .client()
                    .newBuilder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            listener.newResponse(originalResponse, info.file);
                            return originalResponse.newBuilder()
                                    .body(new DownloadResponseBody(originalResponse, listener))
                                    .build();
                        }
                    })
                    .build()
                    .newCall(req)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            OkhttpUtil.log("download", "error:" + e.getMessage());
                            file.delete();
                            isRunning = false;
                            listener.error(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            isRunning = true;
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

                                    File file = info.file;
                                    // 随机访问文件，可以指定断点续传的起始位置
                                    randomAccessFile = new RandomAccessFile(file, "rwd");
                                    randomAccessFile.seek(compeleteSize);
                                    while ((len = bis.read(buff)) != -1) {
                                        randomAccessFile.write(buff, 0, len);
                                    }

                                    // 下载完成
                                    listener.complete(info.file);
                                    isRunning = false;
                                } catch (Exception e) {
                                    info.file.delete();
                                    listener.error(e);
                                    isRunning = false;
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
                                        isRunning = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                isRunning = false;
                                listener.error(new Exception("false," + response.code()));
                            }
                        }
                    });
        }
    }
}
