package com.praheal.mobile.app.screens.patient;

import com.praheal.mobile.app.base.BaseScreen;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class ChoosePatientScreen extends BaseScreen {

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Choose Patient']")
    WebElement choosePatientTitle;

    public ChoosePatientScreen(AndroidDriver androidDriver) {
        super(androidDriver);
    }
}
