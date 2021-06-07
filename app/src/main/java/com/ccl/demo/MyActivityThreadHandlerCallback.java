package com.ccl.demo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;

class MyActivityThreadHandlerCallback implements Handler.Callback {

    Handler mBase;

    public MyActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便而直接使用硬编码
            case 100:
                handleLaunchIntent(msg);
                break;
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchIntent(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;
        // 根据源码，这个msg.obj 对象是 ActivityClientRecord 类型，修改它的intent字段，恢复目标Activity
        try {
            // 把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);

            Intent target = raw.getParcelableExtra(HookUtils.EXTRA_TARGET_INTENT);
            raw.setComponent(target.getComponent());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
