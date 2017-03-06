package com.ider.httprequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView vText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vText = (TextView) findViewById(R.id.textview);

        HttpManager.getInstance().getAsync("http://www.baidu.com", new HttpManager.RequestListener() {
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(String result) {
                vText.setText(result);
            }
        });

    }
}
