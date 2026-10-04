package com.praheal.mobile.app.screens.patient;

import com.praheal.mobile.app.base.BaseSteps;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;

import static org.testng.Assert.assertTrue;

public class ChoosePatientScreenSteps extends BaseSteps {

    private final ChoosePatientScreen choosePatientScreen;

    public ChoosePatientScreenSteps(AndroidDriver androidDriver) {
        super(androidDriver);
        this.choosePatientScreen = new ChoosePatientScreen(androidDriver);
    }

    @Step("Check that Choose Patient dialog is displayed")
    public void checkChoosePatientDialogDisplayed() {
        assertTrue(isElementDisplayed(choosePatientScreen.choosePatientTitle), "Choose Patient dialog is not displayed");
    }
}
