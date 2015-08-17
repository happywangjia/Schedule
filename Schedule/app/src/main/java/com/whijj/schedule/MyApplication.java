package com.whijj.schedule;

import android.app.Application;

/**
 * Created by Administrator on 2015/8/14 0014.
 */
public class MyApplication extends Application{
    private static MyApplication myApplication;
    public final static int LOGIN=10000;
    public final static int LOGINERROR=10001;
    public static MyApplication getContext(){
        return myApplication;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        myApplication=this;
    }

}
