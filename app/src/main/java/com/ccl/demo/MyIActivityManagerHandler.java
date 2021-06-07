package com.ccl.demo;

import android.content.ComponentName;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class MyIActivityManagerHandler implements InvocationHandler {

    private static final String TAG = "MyIActivityManagerHandler";
    Object mBase;

    public MyIActivityManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("startActivity".equals(method.getName())) {
            // 找到参数里面的第一个Intent 对象
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];
            Intent newIntent = new Intent();
            // 替身Activity的包名, 也就是我们自己的包名
            String stubPackage = "com.ccl.demo";
            // 这里我们把启动的Activity临时替换为坑位 StubActivity
            ComponentName componentName = new ComponentName(stubPackage, PitActivity.class.getName());
            newIntent.setComponent(componentName);
            // 把我们原始要启动的TargetActivity先存起来
            newIntent.putExtra(HookUtils.EXTRA_TARGET_INTENT, raw);
            // 替换掉Intent, 达到欺骗AMS的目的
            args[index] = newIntent;
            return method.invoke(mBase, args);
        }
        return method.invoke(mBase, args);
    }
}
