package com.ider.httprequest;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ider-eric on 2017/3/8.
 */

public abstract class DownloadObserver implements Observer<DownloadTask> {

    private DownloadTask task;

    @Override
    public void onNext(DownloadTask task) {
        this.task = task;
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onSubscribe(Disposable d) {

    }
}
