package com.praheal.mobile.capabilities;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static com.praheal.mobile.utils.GetConfigurations.*;

public class DeviceCapabilities {

    public AndroidDriver setAutomatorCapabilities(String appPath, String deviceName) {
        UiAutomator2Options uiAutomator2Options = baseOptions(deviceName).setApp(appPath);
        return createDriver(uiAutomator2Options);
    }

    public AndroidDriver setAutomatorCapabilities(String deviceName) {
        return createDriver(baseOptions(deviceName));
    }

    private UiAutomator2Options baseOptions(String deviceName) {
        UiAutomator2Options uiAutomator2Options = new UiAutomator2Options();
        uiAutomator2Options
                .setPlatformName(getPlatform())
                .setDeviceName(deviceName)
                .setUdid(deviceName)
                .setAutomationName("UiAutomator2")
                .setAppPackage(getPackageName())
                .setAppActivity(getAppActivity())
                .setNoReset(true)
                .setAutoGrantPermissions(true)
                .setAppWaitDuration(Duration.ofSeconds(60))
                .setAdbExecTimeout(Duration.ofSeconds(60));
        uiAutomator2Options.setCapability("ignoreHiddenApiPolicyError", true);
        return uiAutomator2Options;
    }

    private AndroidDriver createDriver(UiAutomator2Options uiAutomator2Options) {
        try {
            return new AndroidDriver(new URL(getAppiumServerUrl()), uiAutomator2Options);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
