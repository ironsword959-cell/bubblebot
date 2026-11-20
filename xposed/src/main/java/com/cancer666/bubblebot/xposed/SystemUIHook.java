package com.cancer666.bubblebot.xposed;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemUIHook implements IXposedHookLoadPackage {

    private static final String SYSTEMUI = "com.android.systemui";
    private static final String TARGET_PKG = "com.cancer666.bubblebot.stealth";
    private static final long LONG_PRESS_MS = 600L;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!SYSTEMUI.equals(lpparam.packageName)) return;
        XposedBridge.log("BubbleBot Xposed: hooking SystemUI");

        String[] candidates = new String[] {
            "com.android.systemui.statusbar.phone.PhoneStatusBarView",
            "com.android.systemui.statusbar.window.StatusBarWindowView",
            "com.android.systemui.statusbar.phone.StatusBarWindowView",
        };

        for (String cls : candidates) {
            try {
                XposedHelpers.findAndHookMethod(
                    cls, lpparam.classLoader, "onTouchEvent", MotionEvent.class,
                    buildHook()
                );
                XposedBridge.log("BubbleBot Xposed: hooked " + cls);
            } catch (Throwable t) {
                XposedBridge.log("BubbleBot Xposed: skip " + cls + " (" + t.getMessage() + ")");
            }
        }
    }

    private XC_MethodHook buildHook() {
        return new XC_MethodHook() {
            private long downAt = 0L;
            private final Handler h = new Handler(Looper.getMainLooper());
            private Runnable pending;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                MotionEvent ev = (MotionEvent) param.args[0];
                int action = ev.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    downAt = System.currentTimeMillis();
                    Context ctx = (Context) XposedHelpers.getObjectField(
                        param.thisObject, "mContext");
                    pending = new Runnable() {
                        @Override public void run() {
                            if (System.currentTimeMillis() - downAt >= LONG_PRESS_MS) {
                                Intent i = new Intent("com.cancer666.bubblebot.TRIGGER");
                                i.setPackage(TARGET_PKG);
                                try {
                                    ctx.sendBroadcast(i);
                                    XposedBridge.log("BubbleBot trigger sent");
                                } catch (Throwable t) {
                                    XposedBridge.log("BubbleBot send fail: " + t.getMessage());
                                }
                            }
                        }
                    };
                    h.postDelayed(pending, LONG_PRESS_MS + 50L);
                } else if (action == MotionEvent.ACTION_UP
                        || action == MotionEvent.ACTION_CANCEL) {
                    if (pending != null) h.removeCallbacks(pending);
                    downAt = 0L;
                }
            }
        };
    }
}
