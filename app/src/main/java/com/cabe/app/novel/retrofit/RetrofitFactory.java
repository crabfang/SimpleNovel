package com.cabe.app.novel.retrofit;

import android.text.TextUtils;
import android.util.Log;

import com.cabe.lib.cache.http.RequestParams;
import com.cabe.lib.cache.http.repository.OkHttpClientFactory;
import com.squareup.okhttp.OkHttpClient;

import java.util.Iterator;
import java.util.Map;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;

/**
 * 作者：沈建芳 on 2018/6/29 17:19
 */
public class RetrofitFactory {
    private static RestAdapter.LogLevel logLevel = RestAdapter.LogLevel.FULL;
    private static OkHttpClient httpClient = generate();
    private static OkHttpClient generate() {
        return OkHttpClientFactory.create();
    }
    private static OkHttpClient getHttpClient() {
        httpClient.setDns(OkHttpDns.getInstance());
        return httpClient;
    }

    protected static <T> T buildApiService(final RequestParams params, Converter converter, Class<T> clazz) {
        String host = params.getHost();
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                Map<String, String> headMap = params.head;
                if(headMap != null) {
                    Iterator<Map.Entry<String, String>> it = headMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> pair = it.next();
                        request.addHeader(pair.getKey(), pair.getValue());
                        it.remove();
                    }
                }
            }
        };
        return getRetrofit(converter, host, requestInterceptor).create(clazz);
    }

    private static RestAdapter getRetrofit(Converter converter, String baseUrl, RequestInterceptor dataInterceptor) {
        if(TextUtils.isEmpty(baseUrl)) {
            throw new RuntimeException("baseUrl is null");
        } else {
            RestAdapter.Builder retrofit = new RestAdapter.Builder();
            retrofit.setLog(new HttpLog());
            retrofit.setLogLevel(logLevel);
            retrofit.setEndpoint(baseUrl);
            if(converter != null) {
                retrofit.setConverter(converter);
            }
            retrofit.setClient(new OkClient(getHttpClient()));
            if(dataInterceptor != null) {
                retrofit.setRequestInterceptor(dataInterceptor);
            }

            return retrofit.build();
        }
    }

    protected static class HttpLog implements RestAdapter.Log {
        @Override
        public void log(String message) {
            Log.i("RxCache.Http", message);
        }
    }
}
