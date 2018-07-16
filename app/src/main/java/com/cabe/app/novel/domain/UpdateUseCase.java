package com.cabe.app.novel.domain;

import android.net.Uri;
import android.text.TextUtils;

import com.cabe.app.novel.utils.DiskUtils;
import com.cabe.lib.cache.exception.ExceptionCode;
import com.cabe.lib.cache.exception.RxException;
import com.cabe.lib.cache.http.RequestParams;
import com.cabe.lib.cache.http.transformer.HttpStringTransformer;
import com.cabe.lib.cache.impl.HttpCacheUseCase;
import com.cabe.lib.cache.interactor.HttpCacheRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.pgyersdk.update.javabean.AppBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：沈建芳 on 2018/7/16 20:24
 */
public class UpdateUseCase extends HttpCacheUseCase<AppBean> {
    public final static String KEY_LAST_PGYER_BUILD = "keyLastPgyerBuild";
    private final static String API_KEY = "38799a299871a7c3d5dc92586cdb1c7f";
    private final static String APP_KEY = "975f6c1c832cb41803173f43db393dce";
    public UpdateUseCase() {
        super(new TypeToken<AppBean>() {}, null);

        RequestParams params = new RequestParams();
        params.host = "https://www.pgyer.com/";
        params.path = "apiv2/app/view";
        params.requestMethod = RequestParams.REQUEST_METHOD_POST;

        Map<String, String> formMap = new HashMap<>();
        formMap.put("_api_key", API_KEY);
        formMap.put("appKey", APP_KEY);
        params.body = formMap;
        setRequestParams(params);

        HttpCacheRepository<String, AppBean> httpRepository = getHttpRepository();
        httpRepository.setResponseTransformer(new HttpStringTransformer<AppBean>() {
            @Override
            public AppBean buildData(String responseStr) {
                JsonObject json = new JsonParser().parse(responseStr).getAsJsonObject();
                if(json != null && json.has("data")) {
                    JsonObject data = json.get("data").getAsJsonObject();
                    int pgyerBuild = data.get("buildBuildVersion").getAsInt();

                    String lastBuildStr = DiskUtils.getData(KEY_LAST_PGYER_BUILD);
                    if(!TextUtils.isEmpty(lastBuildStr)) {
                        int lastPygerBuild = Integer.parseInt(lastBuildStr);
                        if(lastPygerBuild >= pgyerBuild) {
                            throw new RxException(ExceptionCode.RX_EXCEPTION_DEFAULT, "没有新的更新");
                        }
                    }

                    String buildKey = data.get("buildKey").getAsString();
                    int versionCode = data.get("buildVersionNo").getAsInt();
                    String versionName = data.get("buildVersion").getAsString();
                    String updateNote = data.get("buildUpdateDescription").getAsString();
                    String downloadUrl = Uri.parse("https://www.pgyer.com/apiv2/app/install")
                            .buildUpon()
                            .appendQueryParameter("buildKey", buildKey)
                            .appendQueryParameter("_api_key", API_KEY)
                            .appendQueryParameter("appKey", APP_KEY)
                            .build().toString();
                    AppBean appBean = new AppBean();
                    appBean.setDownloadURL(downloadUrl);
                    appBean.setVersionCode(String.valueOf(versionCode));
                    appBean.setVersionName(versionName);
                    appBean.setReleaseNote(updateNote);
                    return appBean;
                }
                return null;
            }
        });
    }
}
