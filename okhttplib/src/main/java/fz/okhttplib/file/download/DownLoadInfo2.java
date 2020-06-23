package fz.okhttplib.file.download;

import java.io.File;
import java.io.IOException;

/**
 * 文件下载信息
 */
public class DownLoadInfo2 {

    public File file, tmpFile;
    public String url;
    private String path, fileName;
    public long totalLength;
    public long currentLength;
    private String tmp = ".tmp";//临时文件后缀

    public DownLoadInfo2(String url, String path, String fileName) {
        this.url = url;
        this.path = path;
        this.fileName = fileName;
        this.currentLength = getCurrentLength();
    }

    private long getCurrentLength() {
        File dir = new File(this.path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.file = new File(dir, this.fileName);
        this.tmpFile = new File(dir, this.fileName + tmp);
        if (!file.exists()) {
            try {
                if (!tmpFile.exists()) tmpFile.createNewFile();
                return tmpFile.length();
            } catch (IOException e) {
                return 0;
            }
        } else {
            return file.length();
        }
    }
}
