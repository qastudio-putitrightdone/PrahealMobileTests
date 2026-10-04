package com.praheal.mobile.login;

import com.praheal.mobile.app.screens.login.LoginScreenSteps;
import com.praheal.mobile.app.screens.patient.ChoosePatientScreenSteps;
import com.praheal.mobile.app.users.PrahealPatient;
import com.praheal.mobile.base.BaseMobileTest;
import com.praheal.mobile.listeners.PrahealLabels;
import io.qameta.allure.*;
import org.testng.annotations.Test;

public class LoginScreenTests extends BaseMobileTest {

    private LoginScreenSteps loginScreenSteps;
    private ChoosePatientScreenSteps choosePatientScreenSteps;

    @Test
    @Epic("Login Screen")
    @Story("Screen Elements Verification")
    @Description("Verify patient login screen displays Mobile No. field")
    @PrahealLabels.TestID("HAT-T11")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyMobileNumberFieldIsDisplayed() {
        loginScreenSteps
                .checkMobileNumberFieldDisplayed();
    }

    @Test
    @Epic("Login Screen")
    @Story("Screen Elements Verification")
    @Description("Verify patient login screen displays Password field")
    @PrahealLabels.TestID("HAT-T12")
    public void verifyPasswordFieldIsDisplayed() {
        loginScreenSteps
                .checkPasswordFieldDisplayed();
    }

    @Test
    @Epic("Login Screen")
    @Story("Screen Elements Verification")
    @Description("Verify patient login screen displays Login button")
    @PrahealLabels.TestID("HAT-T13")
    public void verifyLoginButtonIsDisplayed() {
        loginScreenSteps
                .checkLoginButtonDisplayed();
    }

    @Test
    @Epic("Login Screen")
    @Story("Screen Elements Verification")
    @Description("Verify patient login screen displays Forgot Password? link")
    @PrahealLabels.TestID("HAT-T14")
    public void verifyForgotPasswordIsDisplayed() {
        loginScreenSteps
                .checkForgotPasswordDisplayed();
    }

    @Test(dataProviderClass = LoginDataProvider.class, dataProvider = "userData")
    @Epic("Login Screen")
    @Story("Password Visibility")
    @Description("Verify password entered on patient login screen is masked by default")
    @PrahealLabels.TestID("HAT-T15")
    public void verifyPasswordValueMasked(PrahealPatient prahealPatient) {
        loginScreenSteps
                .enterPassword(prahealPatient.getPassword())
                .checkPasswordFieldIsMasked();
    }

    @Test(dataProviderClass = LoginDataProvider.class, dataProvider = "userData")
    @Epic("Login Screen")
    @Story("Password Visibility")
    @Description("Verify password is displayed when show password icon is clicked on patient login screen")
    @PrahealLabels.TestID("HAT-T16")
    public void verifyPasswordShown(PrahealPatient prahealPatient) {
        loginScreenSteps
                .enterPassword(prahealPatient.getPassword())
                .clickShowHidePasswordIcon()
                .checkPasswordFieldIsNotMasked();
    }

    @Test(dataProviderClass = LoginDataProvider.class, dataProvider = "userData")
    @Epic("Login Screen")
    @Story("Login")
    @Description("Verify Choose Patient dialog is displayed after patient logs in with valid credentials")
    @PrahealLabels.TestID("HAT-T19")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyChoosePatientDialogDisplayedAfterValidLogin(PrahealPatient prahealPatient) {
        loginScreenSteps
                .loginAsPatient(prahealPatient.getMobileNumber(), prahealPatient.getPassword());
        choosePatientScreenSteps
                .checkChoosePatientDialogDisplayed();
    }

    @Test(dataProviderClass = LoginDataProvider.class, dataProvider = "invalidUserData")
    @Epic("Login Screen")
    @Story("Login")
    @Description("Verify error message when patient logs in with invalid mobile number and password")
    @PrahealLabels.TestID("HAT-T20")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyInvalidCredentialsErrorDisplayedAfterInvalidLogin(PrahealPatient prahealPatient) {
        loginScreenSteps
                .loginAsPatient(prahealPatient.getMobileNumber(), prahealPatient.getPassword())
                .checkInvalidCredentialsErrorDisplayed();
    }

    @Test
    @Epic("Login Screen")
    @Story("UI Rendering")
    @Description("Verify patient login screen elements keep their position and size after app is minimised and restored")
    @PrahealLabels.TestID("HAT-T21")
    @Severity(SeverityLevel.NORMAL)
    public void verifyLoginLayoutUnchangedAfterMinimiseAndRestore() {
        loginScreenSteps
                .captureLayout()
                .minimiseAndRestoreApp();
        loginScreenSteps
                .checkLayoutUnchangedSinceCapture();
    }
}
