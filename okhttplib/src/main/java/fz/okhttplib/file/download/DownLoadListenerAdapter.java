package fz.okhttplib.file.download;

import java.io.File;

import okhttp3.Response;

public abstract class DownLoadListenerAdapter implements DownLoadListener {

//    Content-Disposition: attachment;filename=guanfang1.0.9.apk
//    Content-Type: text/plain
    @Override
    public void newResponse(Response response, File file) {

    }

    @Override
    public void cancel() {

    }
}
