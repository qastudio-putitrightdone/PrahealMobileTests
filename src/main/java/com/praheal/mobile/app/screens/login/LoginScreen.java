package com.praheal.mobile.app.screens.login;

import com.praheal.mobile.app.base.BaseScreen;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import static com.praheal.mobile.app.constants.AppIcons.ERROR;
import static com.praheal.mobile.app.constants.AppIcons.SHOW_HIDE_PASSWORD;

public class LoginScreen extends BaseScreen {

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Mobile No.']")
    WebElement mobileNumberInputField;

    @AndroidFindBy(xpath = "//android.widget.EditText[@hint='Password']")
    WebElement passwordInputField;

    @AndroidFindBy(xpath = "//android.view.ViewGroup[@content-desc='" + SHOW_HIDE_PASSWORD + "']")
    WebElement showHidePasswordIcon;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Patient Login']")
    WebElement patientLoginTitle;

    @AndroidFindBy(accessibility = "Login")
    WebElement loginButton;

    @AndroidFindBy(accessibility = "Forgot Password?")
    WebElement forgotPasswordLink;

    @AndroidFindBy(accessibility = "Register As New Patient")
    WebElement registerAsNewPatientButton;

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='" + ERROR + "']"
            + "/following-sibling::android.widget.TextView[1]")
    WebElement errorMessage;

    public LoginScreen(AndroidDriver androidDriver) {
        super(androidDriver);
    }
}
