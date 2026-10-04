package com.praheal.mobile.app.screens.login;

import com.praheal.mobile.app.base.BaseSteps;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.Rectangle;

import java.util.List;
import java.util.Map;

import static com.praheal.mobile.app.constants.ElementAttributes.PASSWORD;
import static com.praheal.mobile.app.constants.ElementAttributes.TEXT;
import static com.praheal.mobile.app.constants.login.LoginConstants.INVALID_CREDENTIALS_MESSAGE;
import static com.praheal.mobile.app.constants.login.LoginConstants.MASKED_ATTRIBUTE;
import static com.praheal.mobile.app.constants.login.LoginConstants.NON_MASKED_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LoginScreenSteps extends BaseSteps {

    private final LoginScreen loginScreen;
    private Map<String, Rectangle> capturedLayout;

    public LoginScreenSteps(AndroidDriver androidDriver) {
        super(androidDriver);
        this.loginScreen = new LoginScreen(androidDriver);
    }

    @Step("Enter Mobile Number")
    public LoginScreenSteps enterMobileNumber(String mobileNumber) {
        loginScreen.mobileNumberInputField.clear();
        loginScreen.mobileNumberInputField.sendKeys(mobileNumber);

        return this;
    }

    @Step("Enter Password")
    public LoginScreenSteps enterPassword(String password) {
        loginScreen.passwordInputField.clear();
        loginScreen.passwordInputField.sendKeys(password);

        return this;
    }

    @Step("Click on Login button")
    public LoginScreenSteps clickOnLoginButton() {
        loginScreen.loginButton.click();

        return this;
    }

    @Step("Login as Patient into app")
    public LoginScreenSteps loginAsPatient(String mobileNumber, String password) {
        return enterMobileNumber(mobileNumber)
                .enterPassword(password)
                .clickOnLoginButton();
    }

    @Step("Click on show/hide password icon")
    public LoginScreenSteps clickShowHidePasswordIcon() {
        loginScreen.showHidePasswordIcon.click();

        return this;
    }

    @Step("Note the position and size of the login form elements")
    public LoginScreenSteps captureLayout() {
        waitUntilVisible(loginScreen.patientLoginTitle);
        capturedLayout = captureRects(loginScreen, LoginFormElement.values());

        return this;
    }

    @Step("Check that login form elements keep their position and size")
    public void checkLayoutUnchangedSinceCapture() {
        waitUntilVisible(loginScreen.patientLoginTitle);
        List<String> differences = layoutDifferences(capturedLayout, captureRects(loginScreen, LoginFormElement.values()));
        assertTrue(differences.isEmpty(), "Login form layout changed: " + differences);
    }

    @Step("Check that Patient Login title is displayed")
    public void checkPatientLoginTitleDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.patientLoginTitle), "Patient Login title is not displayed");
    }

    @Step("Check that Mobile Number field is displayed")
    public void checkMobileNumberFieldDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.mobileNumberInputField), "Mobile Number field is not displayed");
    }

    @Step("Check that Password field is displayed")
    public void checkPasswordFieldDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.passwordInputField), "Password field is not displayed");
    }

    @Step("Check that Login button is displayed")
    public void checkLoginButtonDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.loginButton), "Login button is not displayed");
    }

    @Step("Check that Forgot Password link is displayed")
    public void checkForgotPasswordDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.forgotPasswordLink), "Forgot Password link is not displayed");
    }

    @Step("Check that Register As New Patient button is displayed")
    public void checkRegisterAsNewPatientDisplayed() {
        assertTrue(isElementDisplayed(loginScreen.registerAsNewPatientButton),
                "Register As New Patient button is not displayed");
    }

    @Step("Check that Mobile Number field contains specific value")
    public void checkMobileNumberFieldContainsValue(String value) {
        assertEquals(loginScreen.mobileNumberInputField.getAttribute(TEXT), value,
                "Mobile Number field has unexpected value");
    }

    @Step("Check that Password field is masked")
    public void checkPasswordFieldIsMasked() {
        assertEquals(loginScreen.passwordInputField.getAttribute(PASSWORD), MASKED_ATTRIBUTE,
                "Password field is not masked");
    }

    @Step("Check that Password field is not masked")
    public void checkPasswordFieldIsNotMasked() {
        assertEquals(loginScreen.passwordInputField.getAttribute(PASSWORD), NON_MASKED_ATTRIBUTE,
                "Password field is masked");
    }

    @Step("Check that invalid credentials error message is displayed")
    public void checkInvalidCredentialsErrorDisplayed() {
        assertEquals(loginScreen.errorMessage.getText(), INVALID_CREDENTIALS_MESSAGE,
                "Invalid credentials error message is not displayed");
    }
}
