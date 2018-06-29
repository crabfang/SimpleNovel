package com.cabe.app.novel.retrofit;

import com.cabe.lib.cache.exception.ExceptionCode;
import com.cabe.lib.cache.exception.HttpExceptionCode;
import com.cabe.lib.cache.exception.RxException;
import com.cabe.lib.cache.http.HttpStringCacheManager;
import com.cabe.lib.cache.http.RequestParams;
import com.cabe.lib.cache.http.StreamConverterFactory;
import com.cabe.lib.cache.http.repository.InputStreamHttpFactory;
import com.cabe.lib.cache.http.transformer.HttpStringTransformer;
import com.cabe.lib.cache.http.transformer.Stream2StringTransformer;
import com.cabe.lib.cache.interactor.HttpCacheRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import retrofit.converter.Converter;
import rx.Observable;

/**
 * 作者：沈建芳 on 2018/6/29 17:25
 */
public class MyHttpManager<T> implements HttpCacheRepository<String, T> {private String encode;
    private TypeToken<T> typeToken;
    private Converter converter = null;
    private Observable.Transformer<String, T> transformer = null;

    public MyHttpManager(TypeToken<T> token) {
        this.typeToken = token;
        setStringEncode("gb2312");
        setHttpConverter(StreamConverterFactory.create());
        setResponseTransformer(new HttpStringTransformer<T>() {
            @Override
            public T buildData(String responseStr) {
                if(typeToken == null) {
                    throw RxException.build(ExceptionCode.RX_EXCEPTION_TYPE_UNKNOWN, null);
                }
                if(typeToken.getType() == String.class) {
                    return (T) responseStr;
                } else {
                    try {
                        return new Gson().fromJson(responseStr, typeToken.getType());
                    } catch (Exception e) {
                        throw RxException.build(HttpExceptionCode.RX_EXCEPTION_TYPE_UNKNOWN, e);
                    }
                }
            }
        });
    }
    @Override
    public Observable<T> getHttpObservable(RequestParams params) {
        return new MyHttpFactory().createRequest(params, converter).compose(new Stream2StringTransformer(encode)).compose(transformer);
    }

    @Override
    public void setHttpConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public void setResponseTransformer(Observable.Transformer<String, T> transformer) {
        this.transformer = transformer;
    }

    public void setStringEncode(String encode) {
        this.encode = encode;
    }
}
