package com.ccl.demo;

import android.os.Build;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class HookUtils {
    public static final String EXTRA_TARGET_INTENT = "activity";
    /**
     * Hook AMS
     * 主要完成的操作是，代理Hook AMS 在应用进程的本地代理IActivityManager ，
     * 把真正要启动的Activity临时替换为在AndroidManifest.xml 里坑位Activity
     * 进而骗过AMS
     */
    public static void hookIActivityManager() throws Exception {


        Field amSingletonField =null;
        if (Build.VERSION.SDK_INT >= 26) {
            Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
            amSingletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton");
        }else{
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            amSingletonField = activityManagerNativeClass.getDeclaredField("gDefault");
        }
        amSingletonField.setAccessible(true);

        Object iamSingletonObj = amSingletonField.get(null);

        // 得到iamSingletonObj ，得到iamSingletonObj 对象里的mInstance 字段值，这个值就是那个需要的单例，
        // 就是AMS 在应用进程的本地代理对象
        Class<?> singleton = Class.forName("android.util.Singleton");
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // 原始的 IActivityManager对象
        Object rawIActivityManager = mInstanceField.get(iamSingletonObj);

        // 用动态代理，这里在执行相应方法执行我们的一些逻辑（这里指的是修改Intent 使用坑位Activity ，从而可以越过AMS）
        // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { iActivityManagerInterface }, new MyIActivityManagerHandler(rawIActivityManager));
        mInstanceField.set(iamSingletonObj, proxy);
    }

    /**
     * 由于之前我们用替身欺骗了AMS; 现在我们要换回我们真正需要启动的Activity ，不然就真的启动替身了
     * 到最终要启动Activity的时候,会交给ActivityThread 的一个内部类叫做 H 来完成
     * H 会完成这个消息转发，这里对H 的mCallback 进行处理
     */
    public static void hookActivityThreadHandler() throws Exception {

        // 先获取到当前的ActivityThread 对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        currentActivityThreadField.setAccessible(true);
        Object currentActivityThread = currentActivityThreadField.get(null);

        // 获取ActivityThread 的字段mH 对象
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mH = (Handler) mHField.get(currentActivityThread);

        Field mCallBackField = Handler.class.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);

        // 设置我们的代理CallBack
        mCallBackField.set(mH, new MyActivityThreadHandlerCallback(mH));
    }

}
