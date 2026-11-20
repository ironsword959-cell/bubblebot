package de.robv.android.xposed;

public abstract class XC_MethodHook {

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    public static final class MethodHookParam {
        public Object[] args;
        public Object thisObject;
        public Object getResult() { return null; }
        public void setResult(Object result) {}
        public void setResult(Object result, boolean set) {}
        public Throwable getThrowable() { return null; }
        public void setThrowable(Throwable t) {}
        public boolean hasThrowable() { return false; }
    }

    public static class Unhook {
        public void unhook() {
        }
    }
}
