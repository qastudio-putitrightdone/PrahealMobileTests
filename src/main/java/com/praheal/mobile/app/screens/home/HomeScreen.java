package com.praheal.mobile.app.screens.home;

import com.praheal.mobile.app.base.BaseScreen;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import static com.praheal.mobile.app.constants.AppIcons.CHEVRON_DOWN;

public class HomeScreen extends BaseScreen {

    @AndroidFindBy(xpath = "(//android.widget.TextView[@text='Home'])[1]")
    WebElement homeTitle;

    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='" + CHEVRON_DOWN + "']"
            + "/preceding-sibling::android.widget.TextView[1]")
    WebElement headerPatientName;

    public HomeScreen(AndroidDriver androidDriver) {
        super(androidDriver);
    }
}
