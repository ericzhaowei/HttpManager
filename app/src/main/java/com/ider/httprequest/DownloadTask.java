package com.ider.httprequest;

public class DownloadTask {

    private String fileName;
    private long progress;
    private long total;
    private String url;

    public DownloadTask(String url) {
        this.url = url;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public long getProgress() {
        return progress;
    }

    public long getTotal() {
        return total;
    }

    public String getUrl() {
        return url;
    }
}
