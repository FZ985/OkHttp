package nativedownload.download2;

import java.io.Serializable;

/**
 * Created by JFZ .
 * on 2018/1/17.
 */

public class DownLoadBean implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String SAVE_PATH = "download";
    public String status;
    public String fileName;
    public String downloadUrl;
    public long fileSize;
    public long currentSize;
    public String path;
    public String tempPath;
    public String error;
    public boolean done = false;

    public DownLoadBean(String fileName, String path, String downloadUrl, long fileSize, long currentSize) {
        this.fileName = fileName;
        this.path = path + fileName;
        this.downloadUrl = downloadUrl;
        this.fileSize = fileSize;
        this.currentSize = currentSize;
        this.tempPath = path + fileName.replace(replace(fileName), "") + ".tmp";
    }

    private String replace(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."), fileName.length());
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public synchronized void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public String getPath() {
        return path;
    }

    public String getTempPath() {
        return tempPath;
    }

    @Override
    public String toString() {
        return "DownLoadBean{" +
                "status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", fileSize=" + fileSize +
                ", currentSize=" + currentSize +
                ", path='" + path + '\'' +
                ", tempPath='" + tempPath + '\'' +
                ", error='" + error + '\'' +
                ", done=" + done +
                '}';
    }
}
