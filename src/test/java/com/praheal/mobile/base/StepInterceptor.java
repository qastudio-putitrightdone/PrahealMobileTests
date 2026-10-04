package com.praheal.mobile.base;

import com.praheal.mobile.utils.Screenshots;
import io.appium.java_client.android.AndroidDriver;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class StepInterceptor {

    public static final String CHECK_PREFIX = "check";

    private final AndroidDriver androidDriver;

    StepInterceptor(AndroidDriver androidDriver) {
        this.androidDriver = androidDriver;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @SuperCall Callable<?> superCall) throws Exception {
        StepTracker.enter(method.getName());
        try {
            return superCall.call();
        } finally {
            StepTracker.exit();
            if (method.getName().startsWith(CHECK_PREFIX)) {
                attachScreenshot(method.getName());
            }
        }
    }

    private void attachScreenshot(String methodName) {
        try {
            Screenshots.attach(androidDriver, "Screenshot: " + methodName);
        } catch (Exception ignored) {
        }
    }
}
