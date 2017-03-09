package com.ider.httprequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView vText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vText = (TextView) findViewById(R.id.textview);

//        HttpManager.getInstance().getAsync("http://www.baidu.com", new HttpManager.RequestListener() {
//            @Override
//            public void onError(Exception e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onSuccess(String result) {
//                vText.setText(result);
//            }
//        });



    }

    public void download(View view) {
        DownloadManager.getInstance().download("http://file.antutu.com/soft/antutu-benchmark-V6_3_0.apk", new DownloadObserver() {
            @Override
            public void onNext(DownloadTask task) {
                super.onNext(task);
                vText.setText(task.getProgress() + "/" + task.getTotal());
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }
        });
    }

    public void pause(View view) {
        DownloadManager.getInstance().cancel("http://file.antutu.com/soft/antutu-benchmark-V6_3_0.apk");
    }


}
