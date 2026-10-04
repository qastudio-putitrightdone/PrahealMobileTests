package com.praheal.mobile.app.base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

public abstract class BaseScreen {

    private static final int PAGE_FACTORY_TIMEOUT = 60;

    protected BaseScreen(AndroidDriver androidDriver) {
        PageFactory.initElements(
                new AppiumFieldDecorator(androidDriver, Duration.ofSeconds(PAGE_FACTORY_TIMEOUT)), this
        );
    }
}
