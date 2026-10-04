package com.praheal.mobile.utils;

import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;

import java.io.ByteArrayInputStream;

public class Screenshots {

    public static void attach(AndroidDriver androidDriver, String name) {
        byte[] screenshotBytes = androidDriver.getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), "png");
    }
}
