package fz.okhttplib.file.download;

import java.io.File;

import okhttp3.Response;

interface DownLoadListener {

    void update(long progress, int percent, long contentLength, boolean done);

    void complete(File file);

    void error(Exception e);

    void newResponse(Response response, File file);
}
