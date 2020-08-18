package com.wheatek.ble_demo;

import android.app.Application;
import android.content.Context;
import per.wsj.commonlib.utils.LogUtil;

public class App extends Application {
    private static Context mContext;

    volatile boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }

}