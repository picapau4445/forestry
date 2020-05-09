package com.s4hpi.forestry.counter.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    /**
     * 接続状況を確認する
     * @param context アプリケーションのコンテキスト
     * @return true:接続中 false:未接続
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}
