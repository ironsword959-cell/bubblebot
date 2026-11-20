package com.cancer666.bubblebot.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ModuleEntry implements IXposedHookLoadPackage {
    private final SystemUIHook hook = new SystemUIHook();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        hook.handleLoadPackage(lpparam);
    }
}
