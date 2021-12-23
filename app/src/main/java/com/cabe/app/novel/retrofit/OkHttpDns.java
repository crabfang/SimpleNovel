package com.cabe.app.novel.retrofit;

import android.net.Uri;
import android.util.Log;

import com.cabe.app.novel.model.SourceType;
import com.squareup.okhttp.Dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class OkHttpDns implements Dns {
    private static OkHttpDns instance = null;
    public static OkHttpDns getInstance() {
        if(instance == null) {
            instance = new OkHttpDns();
        }
        return instance;
    }
    private OkHttpDns() {}
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        //通过异步解析接口获取ip
        String bqgHost = Uri.parse(SourceType.BQG.getHost()).getHost();
        if(bqgHost.equals(hostname)) {
            String ip = "68.168.18.81";
            //如果ip不为null，直接使用该ip进行网络请求
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            Log.e("OkHttpDns", "inetAddresses:" + inetAddresses);
            return inetAddresses;
        }
        //如果返回null，走系统DNS服务解析域名
        return Dns.SYSTEM.lookup(hostname);
    }
}