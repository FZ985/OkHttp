package fz.okhttplib.file.download;

import android.annotation.SuppressLint;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadResponseBody2 extends ResponseBody {
    private Response originalResponse;
    private DownLoadListenerAdapter downloadListener;
    private BufferedSource bufferedSource;
    private DownLoadInfo2 info;

    public DownloadResponseBody2(Response originalResponse, DownLoadInfo2 info, DownLoadListenerAdapter downloadListener) {
        this.originalResponse = originalResponse;
        this.downloadListener = downloadListener;
        this.info = info;
    }

    @Override
    public MediaType contentType() {
        return originalResponse.body().contentType();
    }

    @Override
    public long contentLength() {
        return info.totalLength;
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(originalResponse.body().source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = info.currentLength;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;//不断统计当前下载好的数据
                //接口回调
                float percent = (totalBytesRead * 1.0f / contentLength() * 100);
                downloadListener.update(totalBytesRead, number2(percent), contentLength(), bytesRead == -1);
                return bytesRead;
            }
        };
    }

    @SuppressLint("DefaultLocale")
    private float number2(float f) {
        return Float.parseFloat(String.format("%.2f", f));
    }
}
