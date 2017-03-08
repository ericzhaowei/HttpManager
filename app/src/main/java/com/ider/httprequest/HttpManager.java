package com.ider.httprequest;

import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpManager {

    private static final String TAG = "HttpManager";
    
    private static HttpManager INSTANCE;
    private OkHttpClient client;
    private Handler mainHandler;
    private HttpManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    public static HttpManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new HttpManager();
        }
        return INSTANCE;
    }

    public interface RequestListener {
        void onError(Exception e);
        void onSuccess(String result);
    }

    // 异步get请求
    public void getAsync(String url, RequestListener requestListener) {

        Request.Builder builder = new Request.Builder().url(url);
        Request request = builder.build();
        sendRequest(request, requestListener);
    }

    // 同步get请求
    public Call get(String url) throws IOException {
        Request.Builder builder = new Request.Builder().url(url);
        return client.newCall(builder.build());
    }

    public Call get(Request request) {
        return client.newCall(request);
    }

    // 异步post请求
    public void postAsync(String url, Map<String, String> map, RequestListener requestListener) {
        Param[] params = map2Params(map);
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Param param : params) {
            formBuilder.add(param.key, param.value);
        }
        FormBody body = formBuilder.build();

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).post(body).build();
        sendRequest(request, requestListener);

    }

    public void postJsonAsync(String url, String json, RequestListener requestListener) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).post(requestBody).build();
        sendRequest(request, requestListener);
    }


    private void sendRequest(final Request request, final RequestListener requestListener) {
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailureMessage(requestListener, e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String body = response.body().string();
                    sendSuccessMessage(requestListener, body);
                } catch (IOException e) {
                    sendFailureMessage(requestListener, e);
                }
            }
        });

    }

    private void sendFailureMessage(final RequestListener listener, final Exception e) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onError(e);
            }
        });
    }

    private void sendSuccessMessage(final RequestListener listener, final String body) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(body);
            }
        });
    }

    private Param[] map2Params(Map<String, String> map) {
        if(map == null) {
            return new Param[0];
        }
        Set<Map.Entry<String, String>> entries = map.entrySet();
        Param[] params = new Param[entries.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            params[i++] = new Param(entry.getKey(), entry.getValue());
        }
        return params;
    }

    static class Param {
        String key;
        String value;

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }


}
