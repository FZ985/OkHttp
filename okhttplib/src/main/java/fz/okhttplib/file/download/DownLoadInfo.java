package fz.okhttplib.file.download;

import java.io.File;

public class DownLoadInfo {

    public File file;
    public String url, path, fileName;

    public DownLoadInfo(String url, String path, String fileName) {
        this.url = url;
        this.path = path;
        this.fileName = fileName;
    }
}
