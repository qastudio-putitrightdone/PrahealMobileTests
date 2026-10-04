package com.praheal.mobile.app.screens.login;

import com.praheal.mobile.app.base.ScreenElement;
import org.openqa.selenium.WebElement;

import java.util.function.Function;

public enum LoginFormElement implements ScreenElement<LoginScreen> {

    PATIENT_LOGIN_TITLE(screen -> screen.patientLoginTitle),
    MOBILE_NUMBER_FIELD(screen -> screen.mobileNumberInputField),
    PASSWORD_FIELD(screen -> screen.passwordInputField),
    SHOW_PASSWORD_ICON(screen -> screen.showHidePasswordIcon),
    LOGIN_BUTTON(screen -> screen.loginButton),
    FORGOT_PASSWORD_LINK(screen -> screen.forgotPasswordLink),
    REGISTER_AS_NEW_PATIENT_BUTTON(screen -> screen.registerAsNewPatientButton);

    private final Function<LoginScreen, WebElement> element;

    LoginFormElement(Function<LoginScreen, WebElement> element) {
        this.element = element;
    }

    @Override
    public WebElement of(LoginScreen screen) {
        return element.apply(screen);
    }
}
