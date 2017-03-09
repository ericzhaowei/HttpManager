package com.ider.httprequest;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadManager {

    private static final String TAG = "DownloadManager";

    private static final String CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "mDownload";

    private Map<String, Call> dlCalls;

    private static DownloadManager INSTANCE;

    private DownloadManager() {
        dlCalls = new HashMap<>();
        File file = new File(CACHE_PATH);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdir();
        }
    }

    public static DownloadManager getInstance() {
        synchronized (DownloadManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new DownloadManager();
            }
            return INSTANCE;
        }
    }


    public void download(String url, Observer observer) {

        Observable.just(url)
                .filter(s -> !dlCalls.containsKey(url)) // 如果已经包含该下载项就不再重复下载
                .subscribeOn(Schedulers.io())
                .map(this::createDownloadTask)
                .map(this::createRealTask)
                .flatMap(downloadTask -> Observable.create(new DownloadSubscribe(downloadTask)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    private DownloadTask createDownloadTask(String url) {
        DownloadTask task = new DownloadTask(url);
        task.setTotal(getTaskLength(url));
        task.setProgress(0);
        task.setFileName(getFileName(url));
        return task;
    }

    private String getFileName(String url) {
        int index = url.lastIndexOf("/");
        if (index == -1) {
            return url;
        } else {
            return url.substring(index + 1);
        }
    }

    private long getTaskLength(String url) {
        long length = -1;
        try {
            Response response = HttpManager.getInstance().get(url).execute();
            length = response.body().contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }

    private DownloadTask createRealTask(DownloadTask task) {
        DownloadTask realTask = new DownloadTask(task.getUrl());
        realTask.setTotal(task.getTotal());

        String fileName = task.getFileName();
        long contentLength = task.getTotal();
        File file = new File(CACHE_PATH, fileName);
        int i = 1;
        // 文件存在并且已下载完成，则创建新的文件下载
        Log.i(TAG, "createRealTask: exists/content = " + file.length() + "/" + contentLength);
        while (file.length() >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String newFileName;
            if (dotIndex == -1) {
                newFileName = fileName + "(" + i + ")";
            } else {
                String fileType = fileName.substring(dotIndex);
                String fileStart = fileName.substring(0, dotIndex);
                newFileName = fileStart + "(" + i + ")" + fileType;
            }
            file = new File(CACHE_PATH, newFileName);
            i++;
        }
        realTask.setFileName(file.getName());
        realTask.setProgress(file.length());

        return realTask;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe {
        private DownloadTask task;

        private DownloadSubscribe(DownloadTask task) {
            this.task = task;
        }

        @Override
        public void subscribe(ObservableEmitter e) throws Exception {
            e.onNext(task);
            long total = task.getTotal();
            long progress = task.getProgress();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + progress + "-" + total)
                    .url(task.getUrl()).build();
            Call call = HttpManager.getInstance().get(request);
            dlCalls.put(task.getUrl(), call);

            File file = new File(CACHE_PATH, task.getFileName());
            Response response = call.execute();
            InputStream is = null;
            FileOutputStream fos = null;
            Log.i(TAG, "subscribe: start download");
            try {
                is = response.body().byteStream();
                fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                    progress += length;
                    task.setProgress(progress);
                    e.onNext(task);
                }

            } finally {
                try {
                    assert is != null;
                    is.close();
                    assert fos != null;
                    fos.flush();
                    fos.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            dlCalls.remove(task.getUrl());
            e.onComplete();
        }
    }

    public void cancel(String url) {
        Call call = dlCalls.get(url);
        if(call != null) {
            call.cancel();
        }

        dlCalls.remove(url);


    }

}
