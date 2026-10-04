package com.praheal.mobile.utils;

import java.util.Properties;

import static com.praheal.mobile.utils.FetchConfigData.getConfigData;

public class GetConfigurations {

    private static final Properties configValues = getConfigData();

    private static String get(String key) {
        return System.getProperty(key, configValues.getProperty(key));
    }

    public static String getPlatform() {
        return get("platform");
    }

    public static String getPackageName() {
        return get("packageName");
    }

    public static String getAppActivity() {
        return get("appActivity");
    }

    public static String getAppPath() {
        return System.getProperty("user.dir") + "/" + get("appPath");
    }

    public static String getAppiumServerUrl() {
        return get("appiumServerUrl");
    }
}
